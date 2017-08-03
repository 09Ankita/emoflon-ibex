package org.emoflon.moflontohenshin.utils;

import java.util.HashMap;
import java.util.Map;


import org.eclipse.emf.henshin.model.Parameter;

import language.basic.expressions.TGGAttributeExpression;
import language.basic.expressions.TGGEnumExpression;
import language.basic.expressions.TGGExpression;
import language.basic.expressions.TGGLiteralExpression;
import language.inplaceAttributes.TGGInplaceAttributeExpression;


public class ParameterHelper {
	private Map<Parameter, Object> paramterCache;
	
	public ParameterHelper(){
		paramterCache = new HashMap<>();
	}
	
	public void addToCache(Parameter param, TGGExpression expr){
		Object value = null;
		value = handleExpression(expr);		
		paramterCache.put(param, value);
	}
	
	private Object handleExpression(TGGExpression expr){
		if(expr instanceof TGGLiteralExpression){
			return handleLiteralExpression(TGGLiteralExpression.class.cast(expr));
		}else if(expr instanceof TGGAttributeExpression){
			return handleAttributeExpression(TGGAttributeExpression.class.cast(expr));
		}else{
			return handleEnumExpression(TGGEnumExpression.class.cast(expr));
		}
	}
	
	private Object handleEnumExpression(TGGEnumExpression enumExpr){
		return enumExpr.getLiteral().getInstance();
		
	}
	
	private Object handleAttributeExpression(TGGAttributeExpression attrExpr){
		TGGInplaceAttributeExpression inplaceExpr=attrExpr.getObjectVar().getAttrExpr().stream().filter(expr -> expr.getAttribute().getName().equals(attrExpr.getAttribute().getName())).findFirst().get();
		return handleExpression(inplaceExpr.getValueExpr());
	}
	
	private Object handleLiteralExpression(TGGLiteralExpression litExpr){
		return litExpr.getValue();
	}
	
	public Object getValue(Parameter param){
		return paramterCache.get(param);
	}
	
	public <A> A getValue(Parameter param, Class<A> clazz){
		return clazz.cast(getValue(param));
	}
}
