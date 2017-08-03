package org.emoflon.moflontohenshin.utils;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.RuleApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.RuleApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.emf.henshin.model.Rule;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;

public class HenshinHelper {
	private final MoflonToHenshinConfigurator moflonToHenshinConfigurator;
	
	public HenshinHelper(MoflonToHenshinConfigurator moflonToHenshinConfigurator){
		this.moflonToHenshinConfigurator=moflonToHenshinConfigurator;
	}
	
	public void startHenshinAnalysis(Resource resource){
	 	EGraph graph = new EGraphImpl(resource);
	 	
		Module module = MoflonToHenshinUtils.getInstance().getCastItem(resource.getContents(), Module.class);
		
		MoflonToHenshinUtils.getInstance().mapToSubclass(module.getUnits(), Rule.class).forEach(u -> checkRule(u,graph));
	}
	
	private void checkRule(Rule rule, EGraph graph){
		Engine engine = new EngineImpl();
		
		RuleApplication ruleApplication = new RuleApplicationImpl(engine);
		ruleApplication.setEGraph(graph);
		ruleApplication.setRule(rule);
		rule.getParameters().forEach(p -> setParameters(p, ruleApplication));
		
//		engine.
		
	}
	
	private void setParameters(Parameter parameter, RuleApplication ruleApplication){
		ruleApplication.setParameterValue(parameter.getName(), moflonToHenshinConfigurator.getParameterHelper().getValue(parameter));
	}
}
