package org.emoflon.ibex.tgg.core.compiler.pattern.rulepart;

import org.emoflon.ibex.tgg.core.compiler.PatternSuffixes;

import language.BindingType;
import language.DomainType;
import language.TGGRule;
import language.TGGRuleEdge;
import language.TGGRuleElement;
import language.TGGRuleNode;

public class CCPattern extends RulePartPattern {

	public CCPattern(TGGRule rule) {
		super(rule);
	}

	@Override
	protected boolean isRelevantForSignature(TGGRuleElement e) {
		return e.getBindingType() != BindingType.CREATE || e.getDomainType() == DomainType.SRC || e.getDomainType() == DomainType.TRG;
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
	protected String getPatternNameSuffix() {
		return PatternSuffixes.CC;
	}

}
