package org.emoflon.ibex.tgg.operational;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

public interface IOperationalResourceHandler {
	// the metamodel function which is implemented by the user
	void registerUserMetamodels() throws IOException;
	
	// getter function for interface interaction
	ResourceSet getResourceSet();
	String getProjectPath();
	URI getBase();
	Resource getSourceResource();
	Resource getTargetResource();
	
	
	//default resource handling
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

	default Resource createResource(String workspaceRelativePath){
		ResourceSet rs = getResourceSet();
		URI uri = URI.createURI(workspaceRelativePath);
		URI base = getBase();
		URI resolved = uri.resolve(base);
	
		Resource res = null;
		try {
			res = rs.getResource(resolved, true);
		} catch (Exception e) {
			if (res == null) {
				res = rs.createResource(resolved, ContentHandler.UNSPECIFIED_CONTENT_TYPE);
			}
		}
		return res;
	}
	
}
