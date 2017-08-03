package org.emoflon.moflontohenshin.manipulationrules.noderules;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Module;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;

import language.TGGRuleNode;

public class CreateAppAndEngineRule extends NodeCreationRule {

public CreateAppAndEngineRule(MoflonToHenshinConfigurator moflonToHenshinConfigurator) {
		super(HenshinFactory.eINSTANCE.createModule().eClass(), moflonToHenshinConfigurator);
	}

	@Override
	public EObject forceCreation(TGGRuleNode node) {
		Module module = HenshinFactory.eINSTANCE.createModule();		
		return module;
	}

}
