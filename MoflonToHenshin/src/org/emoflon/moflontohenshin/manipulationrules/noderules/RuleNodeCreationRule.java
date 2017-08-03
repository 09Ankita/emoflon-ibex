package org.emoflon.moflontohenshin.manipulationrules.noderules;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Rule;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;

import language.TGGRuleNode;

public class RuleNodeCreationRule extends NodeCreationRule {


	public RuleNodeCreationRule( MoflonToHenshinConfigurator moflonToHenshinConfigurator) {
		super(HenshinFactory.eINSTANCE.createRule().eClass(), moflonToHenshinConfigurator);
	}

	@Override
	public EObject forceCreation(TGGRuleNode node) {
		Rule rule = HenshinFactory.eINSTANCE.createRule(node.getName());
		rule.getMappings();
		rule.getMultiMappings();		
		return rule;
	}

}
