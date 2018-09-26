package org.emoflon.ibex.tgg.compiler.transformations;

import static org.emoflon.ibex.tgg.compiler.patterns.TGGPatternUtil.*;

import java.util.List;

import org.emoflon.ibex.tgg.core.util.TGGModelUtils;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;

import IBeXLanguage.IBeXContextPattern;
import language.BindingType;
import language.DomainType;
import language.TGGComplementRule;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleNode;

public class FWD_OPTPatternTransformation extends OperationalPatternTransformation {

	public FWD_OPTPatternTransformation(ContextPatternTransformation parent, IbexOptions options) {
		super(parent, options);
	}

	@Override
	protected void handleComplementRules(TGGRule rule, IBeXContextPattern ibexPattern) {
		if (rule instanceof TGGComplementRule)
			handleComplementRuleForFWD_OPT((TGGComplementRule) rule, ibexPattern);
	}
	
	private void handleComplementRuleForFWD_OPT(TGGComplementRule rule, IBeXContextPattern ibexPattern) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getPatternName(TGGRule rule) {
		return getFWDOptBlackPatternName(rule.getName());
	}
	
	@Override
	protected void transformNodes(IBeXContextPattern ibexPattern, TGGRule rule) {
		List<TGGRuleNode> nodes = TGGModelUtils.getNodesByOperator(rule, BindingType.CONTEXT);
		nodes.addAll(TGGModelUtils.getNodesByOperatorAndDomain(rule, BindingType.CREATE, DomainType.SRC));
		
		for (final TGGRuleNode node : nodes) {
			parent.transformNode(ibexPattern, node);
		}
		
		// Transform attributes.
		for (final TGGRuleNode node : nodes) {
			parent.transformInNodeAttributeConditions(ibexPattern, node);
		}
	}

	@Override
	protected void transformEdges(IBeXContextPattern ibexPattern, TGGRule rule) {
		List<TGGRuleEdge> edges = TGGModelUtils.getReferencesByOperator(rule, BindingType.CONTEXT);
		edges.addAll(TGGModelUtils.getReferencesByOperatorAndDomain(rule, BindingType.CREATE, DomainType.SRC));
		
		for (TGGRuleEdge edge : edges)
			parent.transformEdge(edges, edge, ibexPattern);

		parent.addContextPattern(ibexPattern, rule);
	}
}
