package org.emoflon.ibex.tgg.operational.strategies.sync;

import java.util.Collection;

import org.emoflon.ibex.tgg.compiler.patterns.PatternSuffixes;
import org.emoflon.ibex.tgg.compiler.patterns.sync.ConsistencyPattern;
import org.emoflon.ibex.tgg.operational.patterns.BWDGreenPattern;
import org.emoflon.ibex.tgg.operational.patterns.EmptyGreenPattern;
import org.emoflon.ibex.tgg.operational.patterns.IGreenPattern;
import org.emoflon.ibex.tgg.operational.patterns.IGreenPatternFactory;

import language.TGGRuleNode;

public class BWD_Strategy extends SYNC_Strategy {

	@Override
	public IGreenPattern revokes(IGreenPatternFactory greenFactory, String patternName, String ruleName) {
		if (patternName.equals(ConsistencyPattern.getName(ruleName)))
			return greenFactory.createGreenPattern(BWDGreenPattern.class);
		else
			return greenFactory.createGreenPattern(EmptyGreenPattern.class);
	}

	@Override
	public Collection<TGGRuleNode> getNodesInOutputDomain(IGreenPattern greenPattern) {
		return greenPattern.getSrcNodes();
	}

	@Override
	public String getSuffix() {
		return PatternSuffixes.BWD;
	}
}
