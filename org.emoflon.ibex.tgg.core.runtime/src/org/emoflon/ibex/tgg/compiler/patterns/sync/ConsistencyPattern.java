package org.emoflon.ibex.tgg.compiler.patterns.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.compiler.patterns.BlackPatternFactory;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexBasePattern;
import org.emoflon.ibex.tgg.compiler.patterns.filter_app_conds.FilterACStrategy;

import language.BindingType;
import language.DomainType;
import language.LanguageFactory;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleNode;
import language.basic.expressions.ExpressionsFactory;
import language.basic.expressions.TGGLiteralExpression;
import language.inplaceAttributes.InplaceAttributesFactory;
import language.inplaceAttributes.TGGAttributeConstraintOperators;
import language.inplaceAttributes.TGGInplaceAttributeExpression;
import runtime.RuntimePackage;

public class ConsistencyPattern extends IbexBasePattern {
	private TGGRuleNode protocolNode;
	private static final String CREATED_SRC_NAME = "createdSrc";
	private static final String CREATED_TRG_NAME = "createdTrg";
	
	private static final String CONTEXT_SRC_NAME = "contextSrc";
	private static final String CONTEXT_TRG_NAME = "contextTrg";
	
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
		
		Collection<TGGRuleEdge> localEdges = determineLocalEdges(protocolNode, signatureNodes);
		Collection<TGGRuleNode> localNodes = Collections.emptyList();
		
		super.initialise(name, signatureNodes, localNodes, localEdges);
	}
	
	protected void createPatternNetwork() {
		addPositiveInvocation(factory.createBlackPattern(WholeRulePattern.class));
		
		if (BlackPatternFactory.strategy != FilterACStrategy.NONE) {
			addPositiveInvocation(factory.createFilterACPatterns(DomainType.SRC));
			addPositiveInvocation(factory.createFilterACPatterns(DomainType.TRG));
		}
	}
	
	public static TGGRuleNode createProtocolNode(TGGRule rule) {
		TGGRuleNode protocolNode = LanguageFactory.eINSTANCE.createTGGRuleNode();
		protocolNode.setName(getProtocolNodeName(rule.getName()));
		protocolNode.setType(RuntimePackage.eINSTANCE.getTGGRuleApplication());
		
		TGGInplaceAttributeExpression tae = InplaceAttributesFactory.eINSTANCE.createTGGInplaceAttributeExpression();
		tae.setAttribute(RuntimePackage.Literals.TGG_RULE_APPLICATION__NAME);
		tae.setOperator(TGGAttributeConstraintOperators.EQUAL);
		
		TGGLiteralExpression le = ExpressionsFactory.eINSTANCE.createTGGLiteralExpression();
		le.setValue("\"" + rule.getName() + "\"");
		
		tae.setValueExpr(le);
		protocolNode.getAttrExpr().add(tae);
		return protocolNode;
	}
	
	public Collection<TGGRuleEdge> determineLocalEdges(TGGRuleNode protocolNode, Collection<TGGRuleNode> signatureNodes) {
		return signatureNodes
				.stream()
				.filter(e -> !e.equals(protocolNode))
				.filter(this::nodeIsConnectedToRuleApplicationNode)
				.map(node ->
				{
					TGGRuleEdge edge = LanguageFactory.eINSTANCE.createTGGRuleEdge();
					boolean context = node.getBindingType() == BindingType.CONTEXT;

					switch (node.getDomainType()) {
					case SRC:
						edge.setName(context? CONTEXT_SRC_NAME : CREATED_SRC_NAME);
						edge.setType(context? RuntimePackage.Literals.TGG_RULE_APPLICATION__CONTEXT_SRC : RuntimePackage.Literals.TGG_RULE_APPLICATION__CREATED_SRC);
						break;

					case TRG:
						edge.setName(context? CONTEXT_TRG_NAME : CREATED_TRG_NAME);
						edge.setType(context? RuntimePackage.Literals.TGG_RULE_APPLICATION__CONTEXT_TRG : RuntimePackage.Literals.TGG_RULE_APPLICATION__CREATED_TRG);
						break;
					default:
						throw new IllegalArgumentException("Domain can only be src or trg!");
					}

					edge.setDomainType(node.getDomainType());
					edge.setBindingType(BindingType.CONTEXT);
					edge.setSrcNode(protocolNode);
					edge.setTrgNode(node);

					return edge;
				})
				.collect(Collectors.toList());
	}

	private boolean nodeIsConnectedToRuleApplicationNode(TGGRuleNode node) {
		return !node.getDomainType().equals(DomainType.CORR);
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
