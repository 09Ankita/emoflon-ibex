package org.emoflon.moflontohenshin.manipulationrules.corrrules;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;
import org.emoflon.moflontohenshin.manipulationrules.AbstractManipulationRule;

import language.TGGRuleNode;

public abstract class CorrCreationRule extends AbstractManipulationRule{

	public CorrCreationRule(MoflonToHenshinConfigurator moflonToHenshinConfigurator){
		super(moflonToHenshinConfigurator);
		this.moflonToHenshinConfigurator.getManipulationHelper().addCorrCreationRule(this);
	}
	
	public abstract boolean needsForcedCreation(TGGRuleNode node, EObject src, EObject trg, Resource corrR);
	public abstract EObject forceCreation(TGGRuleNode node, EObject src, EObject trg, Resource corrR);

}
