package org.emoflon.ibex.tgg.compiler.patterns.common;

import org.emoflon.ibex.tgg.compiler.patterns.PatternFactory;
import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;

import language.BindingType;
import language.DomainType;
import language.TGGRuleEdge;
import language.TGGRuleNode;

public class SrcContextPattern extends IbexPattern {

	public SrcContextPattern(PatternFactory factory) {
		super(factory.getRule());
	}

	@Override
	public boolean isRelevantForSignature(TGGRuleNode e) {
		return e.getDomainType() == DomainType.SRC && e.getBindingType() == BindingType.CONTEXT;
	}

	@Override
	protected String getPatternNameSuffix() {
		return PatternSuffixes.SRC_CONTEXT;
	}

	@Override
	protected boolean isRelevantForBody(TGGRuleEdge e) {
		return e.getDomainType() == DomainType.SRC && e.getBindingType() == BindingType.CONTEXT;
	}

	@Override
	protected boolean isRelevantForBody(TGGRuleNode n) {
		return isRelevantForSignature(n);
	}

	@Override
	protected boolean injectivityIsAlreadyChecked(TGGRuleNode node1, TGGRuleNode node2) {
		return false;
	}

}
