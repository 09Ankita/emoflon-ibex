package org.emoflon.moflontohenshin.manipulationrules.edgerules;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;
import org.emoflon.moflontohenshin.manipulationrules.AbstractManipulationRule;

public abstract class EdgeCreationRule extends AbstractManipulationRule{
	
public EdgeCreationRule(MoflonToHenshinConfigurator moflonToHenshinConfigurator) {
		super(moflonToHenshinConfigurator);
		this.moflonToHenshinConfigurator.getManipulationHelper().addEdgeCreationRule(this);
	}

	public abstract boolean needsForcedCreation(EObject src, EObject trg, EReference ref);
	public abstract void forceCreation(EObject src, EObject trg, EReference ref);
}
