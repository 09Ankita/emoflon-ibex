package org.emoflon.ibex.tgg.compiler.patterns;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.ibex.tgg.compiler.TGGCompiler;
import org.emoflon.ibex.tgg.compiler.patterns.common.ConstraintPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.RulePartPattern;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.EdgeDirection;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.FilterACPattern;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.FilterACStrategy;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.ForbidAllFilterACsPattern;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.SearchEdgePattern;
import org.emoflon.ibex.tgg.compiler.patterns.translation_app_conds.CheckTranslationStatePattern;

import language.BindingType;
import language.DomainType;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleElement;
import language.TGGRuleNode;

public class PatternFactory {
	private TGGRule rule;
	private Map<Object, IbexPattern> patterns;
	private TGGCompiler compiler;
	private static final Collection<CheckTranslationStatePattern> markedPatterns = createMarkedPatterns();
	
	public static final FilterACStrategy strategy = FilterACStrategy.FILTER_NACS;

	public PatternFactory(TGGRule rule, TGGCompiler compiler) {
		this.rule = rule;
		this.compiler = compiler;
		patterns = new HashMap<>();
	}

	public Map<Object, IbexPattern> getPatternMap(){
		return patterns;
	}
	
	public Collection<IbexPattern> getPatterns() {
		return Collections.unmodifiableCollection(patterns.values());
	}
	
	private static Collection<CheckTranslationStatePattern> createMarkedPatterns() {
		List<CheckTranslationStatePattern> markedPatterns = new ArrayList<>();
		
		CheckTranslationStatePattern signProtocolSrcMarkedPattern = new CheckTranslationStatePattern(DomainType.SRC, false);
		CheckTranslationStatePattern signProtocolTrgMarkedPattern = new CheckTranslationStatePattern(DomainType.TRG, false);
		CheckTranslationStatePattern localProtocolSrcMarkedPattern = new CheckTranslationStatePattern(signProtocolSrcMarkedPattern, DomainType.SRC, true);
		CheckTranslationStatePattern localProtocolTrgMarkedPattern = new CheckTranslationStatePattern(signProtocolTrgMarkedPattern, DomainType.TRG, true);
		
		localProtocolSrcMarkedPattern.addTGGPositiveInvocation(signProtocolSrcMarkedPattern);
		localProtocolTrgMarkedPattern.addTGGPositiveInvocation(signProtocolTrgMarkedPattern);
		
		markedPatterns.add(localProtocolSrcMarkedPattern);
		markedPatterns.add(localProtocolTrgMarkedPattern);
		markedPatterns.add(signProtocolSrcMarkedPattern);
		markedPatterns.add(signProtocolTrgMarkedPattern);
		
		return Collections.unmodifiableCollection(markedPatterns);
	}
	
	public static Collection<CheckTranslationStatePattern> getMarkedPatterns() {
		return markedPatterns;
	}
	
	public static IbexPattern getMarkedPattern(DomainType domain, boolean local) {
		return markedPatterns.stream()
							 .filter(p -> p.getDomain().equals(domain) && (p.isLocal() == local))
							 .findFirst()
							 .get();
	}
	
	/**
	 * This method computes constraint patterns for a given pattern to deal with all 0..n multiplicities.
	 * For every created edge in the pattern that has a 0..n multiplicity, a pattern is created which ensures that the multiplicity 
	 * is not violated by applying the rule.  These patterns are meant to be negatively invoked.
	 * 
	 * @param pattern The pattern used for the analysis.
	 * @return All patterns that should be negatively invoked to prevent violations of all 0..n multiplicities.
	 */    
	public Collection<IbexPattern> createPatternsForMultiplicityConstraints(RulePartPattern pattern) {
		TGGRule flattenedRule = compiler.getFlattenedVersionOfRule(rule);
		
        // collect edges that need a multiplicity NAC
        Collection<TGGRuleEdge> relevantEdges = flattenedRule.getEdges().stream()
        													   .filter(e -> e.getType().getUpperBound() > 0
        															   && e.getBindingType() == BindingType.CREATE
        															   && e.getSrcNode().getBindingType() == BindingType.CONTEXT)
        													   .collect(Collectors.toList());

        HashMap<TGGRuleNode, HashSet<EReference>> sourceToProcessedEdgeTypes = new HashMap<TGGRuleNode, HashSet<EReference>>();
        Collection<IbexPattern> negativePatterns = new ArrayList<>();
        for (TGGRuleEdge e : relevantEdges) {
			TGGRuleNode src = e.getSrcNode();
			
			// skip this edge if another edge of same type and with same source has already been processed
			Collection<EReference> processedEdgeTypes = sourceToProcessedEdgeTypes.get(src);
			if (processedEdgeTypes != null && processedEdgeTypes.contains(e.getType())) {
				continue;
			}
			
			// add edge to processed edges for its type and source node
			if (sourceToProcessedEdgeTypes.get(src) == null) {
				sourceToProcessedEdgeTypes.put(src, new HashSet<EReference>());
			}
			sourceToProcessedEdgeTypes.get(src).add(e.getType());
			
			// calculate number of create-edges with the same type coming from this source node
			long similarEdgesCount = flattenedRule.getEdges().stream()
															 .filter(edge -> edge.getType() == e.getType()
															 				&& edge.getSrcNode() == src
															 				&& edge.getBindingType() == BindingType.CREATE)
															 .count();

			Collection<TGGRuleElement> signatureElements = new ArrayList<TGGRuleElement>();
			Collection<TGGRuleElement> bodyElements = new ArrayList<TGGRuleElement>();

			// create/add elements to the pattern
			signatureElements.add(src);

			for (int i = 1; i <= e.getType().getUpperBound()+1-similarEdgesCount; i++) {
				TGGRuleNode trg = EcoreUtil.copy(e.getTrgNode());
				TGGRuleEdge edge = EcoreUtil.copy(e);
				
				trg.setName(trg.getName()+i);
				edge.setSrcNode(src);
				edge.setTrgNode(trg);
				
				bodyElements.add(trg);
				bodyElements.add(edge);
			}

			// create pattern and invocation
			String patternName = e.getSrcNode().getName()
					+ "_"
					+ e.getType().getName()
					+ "Edge"
					+ "_Multiplicity";
			
			negativePatterns.add(createPattern(patternName, () -> new ConstraintPattern(rule, signatureElements, bodyElements, patternName)));
        }
        
        return negativePatterns;
	}

	/**
	 * This method computes constraint patterns to deal with containment references.
	 * For every created containment edge in the given pattern with a context node as target, a pattern
	 * is computed which ensures that the target node is not already contained in another reference.
	 * These patterns are meant to be negatively invoked.
	 * 
	 * @param pattern The pattern for which the negative patterns are computed.
	 * @return All patterns that should be negatively invoked to prevent violations of containment.
	 */
	public Collection<IbexPattern> createPatternsForContainmentReferenceConstraints(RulePartPattern pattern) {
		TGGRule flattenedRule = compiler.getFlattenedVersionOfRule(rule);

        // collect edges that need a multiplicity NAC
        Collection<TGGRuleEdge> relevantEdges = flattenedRule.getEdges().stream()
        													   .filter(e -> e.getType().isContainment()
        															   && e.getBindingType() == BindingType.CREATE
        															   && e.getTrgNode().getBindingType() == BindingType.CONTEXT)
        													   .collect(Collectors.toList());

        Collection<IbexPattern> negativePatterns = new ArrayList<>();
        for (TGGRuleEdge e : relevantEdges) {
			TGGRuleNode trg = e.getTrgNode();
			
			Collection<TGGRuleElement> signatureElements = new ArrayList<TGGRuleElement>();
			Collection<TGGRuleElement> bodyElements = new ArrayList<TGGRuleElement>();

			// create/add elements to the pattern
			TGGRuleNode src = EcoreUtil.copy(e.getSrcNode());
			TGGRuleEdge edge = EcoreUtil.copy(e);
			
			edge.setSrcNode(src);
			edge.setTrgNode(trg);
			
			bodyElements.add(src);
			bodyElements.add(edge);
			signatureElements.add(trg);

			// create pattern and invocation
			String patternName = e.getType().getName()
					+ "Edge_"
					+ e.getTrgNode().getName()
					+ "_Containment";
			
			negativePatterns.add(createPattern(patternName, () -> new ConstraintPattern(rule, signatureElements, bodyElements, patternName)));
        }
        
        return negativePatterns;
	}
		
	public PatternFactory getFactory(TGGRule superRule) {
		return compiler.getFactory(superRule);
	}
	
	public TGGRule getRule() {
		return rule;
	}
	
	public TGGRule getFlattenedVersionOfRule() {
		return compiler.getFlattenedVersionOfRule(rule);
	}
	
	/**********  Generic Pattern Creation Methods ************/
	
	public <T extends IbexPattern> T create(Class<T> c) {
		return tryToCreatePattern(c, () -> {
			try {
				return Optional.of(c.getConstructor(PatternFactory.class).newInstance(this));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private <T extends IbexPattern> T tryToCreatePattern(Object key, Supplier<Optional<T>> creator){
		T p = (T) patterns.computeIfAbsent(key, (k) -> creator.get().orElseThrow(() -> new IllegalStateException("Unable to create pattern")));
		
		if(p == null)
			throw new IllegalStateException("Created pattern is null!");
		
		return p;
	}
	
	private <T extends IbexPattern> T createPattern(Object key, Supplier<T> creator){
		return tryToCreatePattern(key, () -> Optional.of(creator.get()));
	} 

	/**********  Specific Pattern Creation Methods ************/	
	
	public IbexPattern createFilterACPatterns(DomainType domain) {
		return createPattern(rule.getName() + ForbidAllFilterACsPattern.getPatternNameSuffix(domain), () -> new ForbidAllFilterACsPattern(domain, this));
	}

	public IbexPattern createFilterACPattern(TGGRuleNode entryPoint, EReference edgeType, EdgeDirection eDirection, Collection<TGGRule> savingRules) {
		return createPattern(rule.getName() + FilterACPattern.getPatternNameSuffix(entryPoint, edgeType, eDirection), () -> new FilterACPattern(entryPoint, edgeType, eDirection, savingRules, this));
	}

	public IbexPattern createSearchEdgePattern(TGGRuleNode entryPoint, EReference edgeType, EdgeDirection eDirection) {
		return createPattern(rule.getName() + SearchEdgePattern.getPatternNameSuffix(entryPoint, edgeType, eDirection), () -> new SearchEdgePattern(entryPoint, edgeType, eDirection, this));
	}
}