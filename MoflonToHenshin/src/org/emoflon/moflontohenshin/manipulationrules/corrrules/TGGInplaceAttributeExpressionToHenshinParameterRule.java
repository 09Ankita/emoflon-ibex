package org.emoflon.moflontohenshin.manipulationrules.corrrules;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Parameter;
import org.emoflon.ibex.tgg.operational.util.ManipulationUtil;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;

import language.TGGRuleNode;
import language.basic.expressions.TGGExpression;
import language.inplaceAttributes.TGGInplaceAttributeExpression;

public class TGGInplaceAttributeExpressionToHenshinParameterRule extends TGGInplaceAttributeExpressionAbstractRule{

	public TGGInplaceAttributeExpressionToHenshinParameterRule(
			MoflonToHenshinConfigurator moflonToHenshinConfigurator) {
		super(moflonToHenshinConfigurator);
	}

	@Override
	public EObject forceCreation(TGGRuleNode node, EObject src, EObject trg, Resource corrR) {
		TGGInplaceAttributeExpression srcInplaceAE = TGGInplaceAttributeExpression.class.cast(src);
		Parameter trgParameter = Parameter.class.cast(trg);
		
		EAttribute type =srcInplaceAE.getAttribute();
		TGGExpression expr = srcInplaceAE.getValueExpr();
		
		this.setVarName(srcInplaceAE, trgParameter::setName);
		trgParameter.setType(type.getEType());		
		
		this.moflonToHenshinConfigurator.getParameterHelper().addToCache(trgParameter, expr);
		
		return ManipulationUtil.getInstance().defaultCreateCorr(node, src, trg, corrR);
	}

	@Override
	protected boolean getTargetCondition(EObject trg) {
		return trg instanceof Parameter;
	}

}
