package org.emoflon.moflontohenshin;

import org.emoflon.moflontohenshin.manipulationrules.corrrules.TGGEdgeToHenshinEdgeCorrCreationRule;
import org.emoflon.moflontohenshin.manipulationrules.corrrules.TGGInplaceAttributeExpressionToHenshinAttributeRule;
import org.emoflon.moflontohenshin.manipulationrules.corrrules.TGGInplaceAttributeExpressionToHenshinParameterRule;
import org.emoflon.moflontohenshin.manipulationrules.corrrules.TGGNodeToHenshinNodeCorrCreationRule;
import org.emoflon.moflontohenshin.manipulationrules.noderules.CreateAppAndEngineRule;
import org.emoflon.moflontohenshin.manipulationrules.noderules.RuleNodeCreationRule;
import org.emoflon.moflontohenshin.utils.HenshinHelper;
import org.emoflon.moflontohenshin.utils.ManipulationHelper;
import org.emoflon.moflontohenshin.utils.MoflonToHenshinController;
import org.emoflon.moflontohenshin.utils.ParameterHelper;

public class MoflonToHenshinConfigurator {
	
	private ManipulationHelper manipulationHelper;
	private ParameterHelper parameterHelper;
	private HenshinHelper henshinHelper;
	private MoflonToHenshinController moflonToHenshinController;
	
	private MoflonToHenshinConfigurator(){
		this.manipulationHelper = new ManipulationHelper();
		this.parameterHelper = new ParameterHelper();
		this.henshinHelper = new HenshinHelper();
	}
	
	public static MoflonToHenshinConfigurator create(){
		MoflonToHenshinConfigurator instance = new MoflonToHenshinConfigurator();
		instance.init();
		return instance;
	}
	
	private void init(){
		this.moflonToHenshinController = new MoflonToHenshinController(this);
		createAllCreationRules();
	}
	
	private void createAllCreationRules(){
		createNodeCreationRules();
		createEdgeCreationRules();
		createCorrCreationRules();
	}
	
	private void createNodeCreationRules(){
		new RuleNodeCreationRule(this);
		new CreateAppAndEngineRule(this);
	}
	
	private void createEdgeCreationRules(){
		//No EdgeCreationRules Specified
	}
	
	private void createCorrCreationRules(){
		new TGGNodeToHenshinNodeCorrCreationRule(this);
		new TGGEdgeToHenshinEdgeCorrCreationRule(this);
		new TGGInplaceAttributeExpressionToHenshinAttributeRule(this);
		new TGGInplaceAttributeExpressionToHenshinParameterRule(this);
	}
	
	public ManipulationHelper getManipulationHelper(){
		return manipulationHelper;
	}
	
	public ParameterHelper getParameterHelper() {
		return parameterHelper;
	}

	public HenshinHelper getHenshinHelper() {
		return henshinHelper;
	}

	public MoflonToHenshinController getMoflonToHenshinController() {
		return moflonToHenshinController;
	}
	
	
}
