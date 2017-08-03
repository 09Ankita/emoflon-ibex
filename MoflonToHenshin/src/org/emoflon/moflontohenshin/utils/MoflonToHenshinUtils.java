package org.emoflon.moflontohenshin.utils;

import java.util.List;
import java.util.stream.Collectors;

public class MoflonToHenshinUtils {
	private static MoflonToHenshinUtils instance;
	
	private MoflonToHenshinUtils(){
		
	}
	
	public static MoflonToHenshinUtils getInstance(){
		if(instance == null)
			instance = new MoflonToHenshinUtils();
		return instance;
	}
	
	public <A> A getCastItem(List<? super A> list, Class<A> clazz){
		return mapToSubclass(list, clazz).get(0);		
	}
	
	public <A> List<A> mapToSubclass(List<? super A> list, Class<A> clazz){
		return list.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());	
	}

}
