package org.emoflon.ibex.tgg.run;

import java.io.IOException;
import org.emoflon.moflontohenshin.MoflonToHenshinConfigurator;





public class SYNCH_App {

	public static void main(String[] args) throws IOException {	
		MoflonToHenshinConfigurator m2hConfig = MoflonToHenshinConfigurator.create();
		
		System.out.println("Starting SYNCH");
		long tic = System.currentTimeMillis();

		m2hConfig.getMoflonToHenshinController().init("instances/testTGG.xmi", "instances/trg_gen.xmi");
		m2hConfig.getMoflonToHenshinController().transformForward();
		
		long toc = System.currentTimeMillis();
		System.out.println("Completed SYNCH in: " + (toc-tic) + " ms");

	}	

}
