package org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.emoflon.ibex.tgg.compiler.patterns.IbexPatternOptimiser;
import org.emoflon.ibex.tgg.compiler.patterns.PatternFactory;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.RulePartPattern;
import org.emoflon.ibex.tgg.compiler.patterns.translation_app_conds.SrcWithRefinementsPattern;
import org.emoflon.ibex.tgg.compiler.patterns.translation_app_conds.TrgWithRefinementsPattern;

import language.BindingType;
import language.DomainType;
import language.TGG;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleElement;
import language.TGGRuleNode;

public class ForbidAllFilterACsPattern extends RulePartPattern {
	protected DomainType domain;
	protected PatternFactory factory;
	protected IbexPatternOptimiser optimiser;

	public ForbidAllFilterACsPattern(DomainType domain, PatternFactory factory) {
		super(factory.getFlattenedVersionOfRule());
		this.factory = factory;
		this.domain = domain;
		optimiser = new IbexPatternOptimiser();

		initialize();
		
		createPatternNetwork();
	}
	
	protected void createPatternNetwork() {
		addDECPatternsAsTGGNegativeInvocations(rule, domain);
		
		switch (domain) {
		case SRC:
			addTGGPositiveInvocation(factory.create(SrcWithRefinementsPattern.class));			
			break;

		case TRG:
			addTGGPositiveInvocation(factory.create(TrgWithRefinementsPattern.class));			
			break;
			
		default:
			throw(new IllegalStateException("No handling for CORR domain"));
		}
	}

	protected void addDECPatternsAsTGGNegativeInvocations(TGGRule rule, DomainType domain) {
		final Collection<IbexPattern> filterNACs = new ArrayList<>();
		
		for (TGGRuleNode n : rule.getNodes()) {
			EClass nodeClass = n.getType();

			if (nodeIsNotTranslatedByThisRule(n)) continue;
			if (nodeIsNotRelevant(domain, n)) continue;

			// Create DECPatterns as negative children in the network
			for (EReference eType : FilterACHelper.extractEReferences(nodeClass)) {
				for (EdgeDirection eDirection : EdgeDirection.values()) {
					TGG tgg = (TGG) rule.eContainer();

					if (typeDoesNotFitToDirection(n, eType, eDirection)) continue;
					if (onlyPossibleEdgeIsAlreadyTranslatedInRule(n, eType, eDirection)) continue;
					if (edgeIsNeverTranslatedInTGG(domain, eType, eDirection, tgg)) continue;
		
					// Collect all Filter NACs, but do not add them yet as negative invocations
					if(thereIsNoSavingRule(domain, eType, eDirection, tgg))
						filterNACs.add(factory.createFilterACPattern(n, eType, eDirection));					
				}
			}
		}
		
		// Use optimiser to remove some of the filter NACs
		final Collection<IbexPattern> optimisedFilterNACs = filterNACs.stream()
							   .filter(nac -> !optimiser.isRedundantDueToEMFContainmentSemantics(nac))
							   .collect(Collectors.toList());
		
		optimisedFilterNACs.removeAll(optimiser.ignoreDueToEOppositeSemantics(optimisedFilterNACs));
		
		// Add all remaining filter NACs now as negative invocations
		addTGGNegativeInvocations(optimisedFilterNACs);
	}

	private boolean thereIsNoSavingRule(DomainType domain, EReference eType, EdgeDirection eDirection, TGG tgg) {
		return determineSavingRules(domain, eType, eDirection, tgg).isEmpty();
	}

	private boolean edgeIsNeverTranslatedInTGG(DomainType domain, EReference eType, EdgeDirection eDirection, TGG tgg) {
		return !FilterACHelper.isEdgeInTGG(tgg, eType, eDirection, false, domain);
	}

	private boolean onlyPossibleEdgeIsAlreadyTranslatedInRule(TGGRuleNode n, EReference eType, EdgeDirection eDirection) {
		int numOfEdges = FilterACHelper.countEdgeInRule(rule, n, eType, eDirection, false, domain).getLeft();
		return eType.getUpperBound() == 1 && numOfEdges == 1;
	}

	private boolean typeDoesNotFitToDirection(TGGRuleNode n, EReference eType, EdgeDirection eDirection) {
		return !FilterACHelper.getType(eType, eDirection).equals(n.getType());
	}

	private boolean nodeIsNotTranslatedByThisRule(TGGRuleNode n) {
		return !n.getBindingType().equals(BindingType.CREATE);
	}

	private boolean nodeIsNotRelevant(DomainType domain, TGGRuleNode n) {
		return !n.getDomainType().equals(domain) || n.getDomainType().equals(DomainType.CORR);
	}

	private List<TGGRule> determineSavingRules(DomainType domain, EReference eType, EdgeDirection eDirection, TGG tgg) {
		return tgg.getRules().stream()
				.filter(r -> isSavingRule(domain, eType, eDirection, r))
				.collect(Collectors.toList());
	}

	private boolean isSavingRule(DomainType domain, EReference eType, EdgeDirection eDirection, TGGRule r) {
		return FilterACHelper.countEdgeInRule(r, eType, eDirection, true, domain).getLeft() > 0;
	}

	@Override
	protected boolean injectivityIsAlreadyChecked(TGGRuleNode node1, TGGRuleNode node2) {
		return true;
	}

	@Override
	protected boolean isRelevantForBody(TGGRuleEdge e) {
		return false;
	}

	@Override
	protected boolean isRelevantForBody(TGGRuleNode n) {
		return false;
	}

	@Override
	public boolean isRelevantForSignature(TGGRuleElement e) {
		return e.getDomainType() == domain;
	}

	@Override
	protected String getPatternNameSuffix() {
		return getPatternNameSuffix(domain);
	}
	
	public static String getPatternNameSuffix(DomainType domain){
		return PatternSuffixes.NO_FILTER_ACs(domain);
	}

	public boolean isEmpty() {
		return getPositiveInvocations().isEmpty() && getNegativeInvocations().isEmpty();
	}
}
