package org.emoflon.ibex.tgg.operational;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

public interface IOperationalResourceHandler {
	void registerUserMetamodels() throws IOException;
	
	ResourceSet getResourceSet();
	
	URI getBase();
	
	default void loadAndRegisterMetamodel(String workspaceRelativePath) throws IOException {
		Resource res = loadResource(workspaceRelativePath);
		EPackage pack = (EPackage) res.getContents().get(0);
		getResourceSet().getPackageRegistry().put(res.getURI().toString(), pack);
		getResourceSet().getResources().remove(res);
	}
	
	default Resource loadResource(String workspaceRelativePath) throws IOException {
		Resource res = createResource(workspaceRelativePath);
		res.load(null);
		EcoreUtil.resolveAll(res);
		return res;
	}

	default Resource createResource(String workspaceRelativePath) {
		URI uri = URI.createURI(workspaceRelativePath);
		Resource res = getResourceSet().createResource(uri.resolve(getBase()), ContentHandler.UNSPECIFIED_CONTENT_TYPE);
		return res;
	}
	
}
