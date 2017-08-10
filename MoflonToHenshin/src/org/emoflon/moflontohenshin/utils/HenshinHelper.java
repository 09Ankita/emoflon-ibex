package org.emoflon.moflontohenshin.utils;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.UnsupportedRuleException;

import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.cpa.result.CriticalPair;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;

import language.LanguageFactory;

public class HenshinHelper {
	
	private CPAOptions cpaOptions;
	
	public HenshinHelper(){
		initCPAEngine();
	}
	
	public void setOptions(boolean complete, boolean ignoreIdenticalRules, boolean reduceSameRuleAndSameMatch){
		cpaOptions.setComplete(complete);
		cpaOptions.setIgnore(ignoreIdenticalRules);
		cpaOptions.setReduceSameRuleAndSameMatch(reduceSameRuleAndSameMatch);
	}
	
	private void initCPAEngine(){
		cpaOptions = new CPAOptions();
		setOptions(true, true, true);
	}
	
	public CPAResult startHenshinAnalysis(Resource resource, boolean computeDependencies, boolean computeConflicts) {
		CpaByAGG cpaEngine = new CpaByAGG();

		Module module = MoflonToHenshinUtils.getInstance().getCastItem(resource.getContents(), Module.class);

		List<Rule> rules = MoflonToHenshinUtils.getInstance().mapToSubclass(module.getUnits(), Rule.class);

		
		
		try {
			cpaEngine.init(rules, cpaOptions);
		} catch (UnsupportedRuleException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		CPAResult conflictResult = null;
		CPAResult dependencyResult = null;

		if (computeDependencies) {
			dependencyResult = cpaEngine.runDependencyAnalysis();
		}

		if (computeConflicts) {
			conflictResult = cpaEngine.runConflictAnalysis();
		}

		return joinCPAResults(conflictResult, dependencyResult);
	}
	
		private CPAResult joinCPAResults(CPAResult conflictResult, CPAResult dependencyResult) {
			// if only conflicts or dependencies exist just return those.
			if (conflictResult == null)
				return dependencyResult;
			if (dependencyResult == null)
				return conflictResult;

			// join the conflicts and dependencies
			CPAResult cpaResult = new CPAResult();
			if (conflictResult != null && dependencyResult != null) {
				List<CriticalPair> conflCriticalPairs = conflictResult.getCriticalPairs();
				for (CriticalPair critPair : conflCriticalPairs) {
					cpaResult.addResult(critPair);
				}
				List<CriticalPair> depCriticalPairs = dependencyResult.getCriticalPairs();
				for (CriticalPair critPair : depCriticalPairs) {
					cpaResult.addResult(critPair);
				}
			}
			return cpaResult;
		}
	
	

}
