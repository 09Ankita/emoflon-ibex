package org.emoflon.ibex.tgg.operational;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;

public abstract class RuleOperations {
	protected IModelManipulations manipulator;
	protected Map<String, EPackage> packages;
	protected Resource rSrc; 
	protected Resource rCorr;
	protected Resource rTrg;

	public RuleOperations(IModelManipulations manipulator, Resource src, Resource corr, Resource trg){
		this.manipulator = manipulator;
		this.packages = new HashMap<>();
		this.rSrc = src;
		this.rCorr = corr;
		this.rTrg = trg;
	}
	
	protected void register(String uri, String key) throws IOException{
		packages.put(key, packageFor(uri));
	}
	
	protected void link(EObject s, String featureName, Object t) {
		s.eSet(s.eClass().getEStructuralFeature(featureName), t);
	}
	
	protected EObject createSrc(String eClass, String key) throws ModelManipulationException{
		return manipulator.create(rSrc, eClassFor(eClass, key));
	}
	
	protected EObject createCorr(String eClass, String key, Object src, Object trg) throws ModelManipulationException{
		EObject corr =  manipulator.create(rCorr, eClassFor(eClass, key));
		manipulator.add(corr, corr.eClass().getEStructuralFeature("source"), src);
		manipulator.add(corr, corr.eClass().getEStructuralFeature("target"), trg);
		return corr;
	}
	
	protected EObject createTrg(String eClass, String key) throws ModelManipulationException{
		return manipulator.create(rTrg, eClassFor(eClass, key));
	}
	
	private EClass eClassFor(String eClass, String key) {
		return (EClass)packages.get(key).getEClassifier(eClass);
	}

	private EPackage packageFor(String uri) throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		Resource r = rs.createResource(URI.createURI(uri));
		r.load(null);
		return (EPackage) r.getContents().get(0);
	}
}
