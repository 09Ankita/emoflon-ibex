package org.emoflon.moflontohenshin.utils;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.HenshinPackage;
import org.emoflon.ibex.tgg.operational.OperationMode;
import org.emoflon.ibex.tgg.operational.TGGRuntimeUtil;
import org.emoflon.ibex.tgg.operational.csp.constraints.factories.MoflonToHenshinAttrCondDefLibrary;
import org.emoflon.ibex.tgg.run.MoflonToHenshinTransformation;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;
import org.moflon.core.utilities.eMoflonEMFUtil;

import language.LanguagePackage;
import language.TGG;
import runtime.RuntimePackage;

public class MoflonToHenshinController {
	
	private final MoflonToHenshinConfigurator moflonToHenshinConfigurator;
	
	private boolean initialized;
	
	private TGGRuntimeUtil tggRuntime;
	
	private Resource srcRes, trgRes, protocolRes, corrRes;
	
	private ResourceSet resSet;
	
	public MoflonToHenshinController(MoflonToHenshinConfigurator moflonToHenshinConfigurator){
		this.moflonToHenshinConfigurator=moflonToHenshinConfigurator;
		initialized=false;
	}
	
	public boolean init(String srcPath, String trgPath){
		
		String pluginID = registerResourceSet();
		Resource tggR = resSet.getResource(URI.createFileURI("model/MoflonToHenshin.tgg.xmi"), true);
		registerResourceSet();
		TGG tgg = (TGG) tggR.getContents().get(0);
		
		// create your resources 
		srcRes = resSet.createResource(URI.createFileURI(srcPath));
		trgRes = resSet.createResource(URI.createFileURI(trgPath));
		corrRes = resSet.createResource(URI.createFileURI("instances/corr_gen.xmi"));
		protocolRes = resSet.createResource(URI.createFileURI("instances/protocol_gen.xmi"));
		
		//TGG source = (TGG) tggR.getContents().get(0);

		tggRuntime = new TGGRuntimeUtil(tgg, srcRes, corrRes, trgRes, protocolRes, pluginID);
		tggRuntime.getCSPProvider().registerFactory(new MoflonToHenshinAttrCondDefLibrary());
		
		initialized = true;
		return initialized;
	}
	
	private String registerResourceSet(){
		resSet = eMoflonEMFUtil.createDefaultResourceSet();
		return registerMetamodels(resSet);
	}
	
	public void transformForward() throws IOException{
		transform(OperationMode.FWD, srcRes, trgRes);
		this.moflonToHenshinConfigurator.getHenshinHelper().startHenshinAnalysis(trgRes, true, true);
		int i =0;
		i++;
	}
	
	public void transformBackwards() throws IOException{
		transform(OperationMode.BWD, trgRes, srcRes);
	}
	
	private void transform(OperationMode om, Resource source, Resource target) throws IOException{
		if(initialized){
			source.load(null);
			EcoreUtil.resolveAll(source);
			
			tggRuntime.setMode(om);
			
			 MoflonToHenshinTransformation transformation = new MoflonToHenshinTransformation(resSet, tggRuntime);
			
			transformation.execute();
			
			tggRuntime.run();
			
			transformation.dispose();
			
			corrRes.save(null);
			protocolRes.save(null);
			target.save(null);
		}else{
			throw new RuntimeException("TGG has not been initialized!!");
		}
	}
	
	private String registerMetamodels(ResourceSet rs){
		// Register internals
		LanguagePackage.eINSTANCE.getName();
		RuntimePackage.eINSTANCE.getName();
		HenshinPackage.eINSTANCE.getName();

		
		// Add mapping for correspondence metamodel
		Resource corr = rs.getResource(URI.createFileURI("model/MoflonToHenshin.ecore"), true);
		EPackage pcorr = (EPackage) corr.getContents().get(0);
		Registry.INSTANCE.put(corr.getURI().toString(), corr);
		Registry.INSTANCE.put("platform:/resource/MoflonToHenshin/model/MoflonToHenshin.ecore", pcorr);
		Registry.INSTANCE.put("platform:/plugin/MoflonToHenshin/model/MoflonToHenshin.ecore", pcorr);
		EPackage tggEPackage = LanguagePackage.eINSTANCE;
		Registry.INSTANCE.put("platform:/plugin/org.emoflon.ibex.tgg.core.language/model/Language.ecore", tggEPackage);		
		EcoreUtil.resolveAll(rs);
		return pcorr.getNsPrefix();
	}
	
}
