package org.emoflon.ibex.tgg.core.compiler;

import org.eclipse.emf.ecore.EReference;

public class EdgePatternNaming {
	
	private static final String EMFEdge = "EMFEdge";
	
	private static final String EdgeWrapper = "EdgeWrapper";
	
	private static final String MissingEdgeWrapper = "Missing" + EdgeWrapper;
	
	private static final String ExistingEdgeWrapper = "Existing" + EdgeWrapper;
	
	public static String getSuffix(EReference ref){
		return ref.getEContainingClass().getName() + "_" + ref.getEType().getName() + "_eMoflonEdgeWrapper_" + ref.getName();
	}
	
	public static String getEMFEdge(EReference ref){
		return EMFEdge + getSuffix(ref);
	}
	
	public static String getEdgeWrapper(EReference ref){
		return EdgeWrapper + getSuffix(ref);
	}
	
	public static String getMissingEdgeWrapper(EReference ref){
		return MissingEdgeWrapper + getSuffix(ref);
	}
	
	public static String getExistingEdgeWrapper(EReference ref){
		return ExistingEdgeWrapper + getSuffix(ref);
	}
	
}