package org.emoflon.ibex.common.utils;

import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/**
 * Utility methods for manipulating EMF models.
 */
public class EMFManipulationUtils {

	/**
	 * Checks whether the given object is a dangling node.
	 * 
	 * @param eObject
	 *            the node
	 * @return true if the node is a dangling node
	 */
	public static boolean isDanglingNode(final EObject eObject) {
		return eObject.eResource() == null;
	}

	/**
	 * Checks whether the given object is a dangling node.
	 * 
	 * @param eObject
	 *            an {@link Optional} for a node
	 * @return true if the node is a dangling node; false if the node is not a
	 *         dangling node or the Optional is not present
	 */
	public static boolean isDanglingNode(Optional<EObject> eObject) {
		return eObject.map(EMFManipulationUtils::isDanglingNode).orElse(false);
	}

	/**
	 * Creates a reference between the two objects
	 * 
	 * @param source
	 *            the source node
	 * @param target
	 *            the target node
	 * @param reference
	 *            the edge type
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void createEdge(final EObject source, final EObject target, final EReference reference) {
		if (reference.isMany()) {
			((EList) source.eGet(reference)).add(target);
		} else {
			source.eSet(reference, target);
		}
	}

	/**
	 * Delete the given EReference between the two objects.
	 * 
	 * @param source
	 *            the source node
	 * @param target
	 *            the target node
	 * @param reference
	 *            the edge type
	 */
	@SuppressWarnings("rawtypes")
	public static void deleteEdge(final EObject source, final EObject target, final EReference reference) {
		if (source.eResource() == null) {
			return;
		}
		if (reference.isMany()) {
			EList list = ((EList) source.eGet(reference));
			if (list.contains(target)) {
				list.remove(target);
			}
		} else {
			if (source.eGet(reference) != null) {
				source.eUnset(reference);
			}
		}
	}
}