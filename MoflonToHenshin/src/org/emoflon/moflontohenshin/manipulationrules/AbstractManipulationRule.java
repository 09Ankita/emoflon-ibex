package org.emoflon.moflontohenshin.manipulationrules;

import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;

public abstract class AbstractManipulationRule {
	final protected MoflonToHenshinConfigurator moflonToHenshinConfigurator;
	
	public AbstractManipulationRule(MoflonToHenshinConfigurator moflonToHenshinConfigurator) {
		this.moflonToHenshinConfigurator=moflonToHenshinConfigurator;
	}
}
