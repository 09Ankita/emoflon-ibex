package org.emoflon.ibex.tgg.compiler.patterns.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.ibex.tgg.compiler.patterns.BlackPatternFactory;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.common.IBlackPattern;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexBasePattern;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.FilterNACStrategy;

import language.BindingType;
import language.DomainType;
import language.LanguageFactory;
import language.TGGAttributeConstraintOperators;
import language.TGGInplaceAttributeExpression;
import language.TGGLiteralExpression;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleNode;
import runtime.RuntimePackage;

public class ConsistencyPattern extends IbexBasePattern {
	private TGGRuleNode protocolNode;
	
	public ConsistencyPattern(BlackPatternFactory factory) {
		super(factory);
		initialise(factory.getFlattenedVersionOfRule());
		createPatternNetwork();
	}
	
	protected void initialise(TGGRule rule) {
		String name = getName(rule.getName());

		protocolNode = createProtocolNode(rule);

		Collection<TGGRuleNode> signatureNodes = new ArrayList<>(rule.getNodes());
		signatureNodes.add(protocolNode);
		
		Collection<TGGRuleEdge> localEdges = Collections.emptyList();
		Collection<TGGRuleNode> localNodes = Collections.emptyList();
		
		super.initialise(name, signatureNodes, localNodes, localEdges);
	}
	
	protected void createPatternNetwork() {
		createMarkedInvocations();
		addPositiveInvocation(factory.createBlackPattern(WholeRulePattern.class));
		
		if (factory.getOptions().getFilterNACStrategy() != FilterNACStrategy.NONE) {
			addPositiveInvocation(factory.createFilterACPatterns(DomainType.SRC));
			addPositiveInvocation(factory.createFilterACPatterns(DomainType.TRG));
		}
	}
	
	public static TGGRuleNode createProtocolNode(TGGRule rule) {
		TGGRuleNode protocolNode = LanguageFactory.eINSTANCE.createTGGRuleNode();
		protocolNode.setName(getProtocolNodeName(rule.getName()));
		protocolNode.setType(RuntimePackage.eINSTANCE.getTGGRuleApplication());
		
		TGGInplaceAttributeExpression tae = LanguageFactory.eINSTANCE.createTGGInplaceAttributeExpression();
		tae.setAttribute(RuntimePackage.Literals.TGG_RULE_APPLICATION__NAME);
		tae.setOperator(TGGAttributeConstraintOperators.EQUAL);
		
		TGGLiteralExpression le = LanguageFactory.eINSTANCE.createTGGLiteralExpression();
		le.setValue("\"" + rule.getName() + "\"");
		
		tae.setValueExpr(le);
		protocolNode.getAttrExpr().add(tae);
		return protocolNode;
	}
	
	public void createMarkedInvocations() {
		TGGRuleNode ruleApplicationNode = getRuleApplicationNode(getSignatureNodes());
		
		signatureNodes
		.stream()
		.filter(e -> !e.equals(ruleApplicationNode))
		.forEach(el ->
		{
			TGGRuleNode node = (TGGRuleNode) el;
			if (nodeIsConnectedToRuleApplicationNode(node)) {
				IBlackPattern markedPattern = getPatternFactory().getMarkedPattern(node.getDomainType(), node.getBindingType().equals(BindingType.CONTEXT));
				TGGRuleNode invokedRuleApplicationNode = getRuleApplicationNode(markedPattern.getSignatureNodes());
				TGGRuleNode invokedObject = (TGGRuleNode) markedPattern.getSignatureNodes()
						.stream()
						.filter(e -> !e.equals(invokedRuleApplicationNode))
						.findFirst()
						.get();
				
				Map<TGGRuleNode, TGGRuleNode> mapping = new HashMap<>();
				mapping.put(ruleApplicationNode, invokedRuleApplicationNode);
				mapping.put(node, invokedObject);
				
				addPositiveInvocation(markedPattern, mapping);
			}
		});
	}

	private boolean nodeIsConnectedToRuleApplicationNode(TGGRuleNode node) {
		return !node.getDomainType().equals(DomainType.CORR);
	}

	private TGGRuleNode getRuleApplicationNode(Collection<TGGRuleNode> elements) {
		return elements.stream()
				.filter(this::isRuleApplicationNode)
				.findAny()
				.get();
	}

	private boolean isRuleApplicationNode(TGGRuleNode n) {
		return n.getType().equals(RuntimePackage.eINSTANCE.getTGGRuleApplication());
	}
	
	public static String getProtocolNodeName(String ruleName) {
		return ruleName + protocolNodeSuffix;
	}
	
	public static final String protocolNodeSuffix = "_eMoflon_ProtocolNode";

	@Override
	protected boolean injectivityIsAlreadyChecked(TGGRuleNode node1, TGGRuleNode node2) {
		return true;
	}

	public static String getName(String ruleName) {
		return ruleName + PatternSuffixes.CONSISTENCY;
	}
}
