package org.emoflon.ibex.tgg.operational.util;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.gervarro.democles.common.IDataFrame;
import org.gervarro.democles.specification.emf.Pattern;

public class IMatch {
	private IDataFrame frame;
	private Pattern pattern;

	public IMatch(IDataFrame frame, Pattern pattern) {
		this.frame = frame;
		this.pattern = pattern;
	}
	
	public Collection<String> parameterNames() {
		return pattern.getSymbolicParameters()
				.stream()
				.map(p -> p.getName())
				.collect(Collectors.toList());
	}

	public EObject get(String name) {
		int index = varNameToIndex(name);
		return (EObject) frame.getValue(index);
	}

	public String patternName() {
		return pattern.getName();
	}
	
	private int varNameToIndex(String varName) {
		for(int i = 0; i <= pattern.getSymbolicParameters().size(); i++){
			if(varName.equals(pattern.getSymbolicParameters().get(i)))
				return i;
		}
		
		return -1;
	}
}
