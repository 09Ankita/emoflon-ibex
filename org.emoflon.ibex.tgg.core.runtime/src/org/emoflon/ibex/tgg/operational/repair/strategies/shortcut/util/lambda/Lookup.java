package org.emoflon.ibex.tgg.operational.repair.strategies.shortcut.util.lambda;

import org.eclipse.emf.ecore.EObject;

@FunctionalInterface
public interface Lookup {
	Object lookup(EObject source);
}