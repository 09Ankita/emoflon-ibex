package org.emoflon.ibex.gt.engine;

import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.ibex.common.operational.ICreatePatternInterpreter;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.utils.EMFManipulationUtils;

import IBeXLanguage.IBeXAttributeAssignment;
import IBeXLanguage.IBeXAttributeParameter;
import IBeXLanguage.IBeXAttributeValue;
import IBeXLanguage.IBeXConstant;
import IBeXLanguage.IBeXCreatePattern;
import IBeXLanguage.IBeXEnumLiteral;

/**
 * Interpreter applying creation of elements for graph transformation.
 */
public class GraphTransformationCreateInterpreter implements ICreatePatternInterpreter {
	private Resource defaultResource;

	/**
	 * Creates a new GraphTransformationCreateInterpreter.
	 * 
	 * @param defaultResource
	 *            the default resource
	 */
	public GraphTransformationCreateInterpreter(final Resource defaultResource) {
		this.defaultResource = defaultResource;
	}

	@Override
	public Optional<IMatch> apply(final IBeXCreatePattern createPattern, final IMatch match,
			final Map<String, Object> parameters) {
		// Create nodes and edges.
		createPattern.getCreatedNodes().forEach(node -> {
			EObject newNode = EcoreUtil.create(node.getType());
			this.defaultResource.getContents().add(newNode);
			match.put(node.getName(), newNode);
		});
		createPattern.getCreatedEdges().forEach(edge -> {
			EObject src = (EObject) match.get(edge.getSourceNode().getName());
			EObject trg = (EObject) match.get(edge.getTargetNode().getName());
			EMFManipulationUtils.createEdge(src, trg, edge.getType());
		});

		// Assign attributes.
		createPattern.getAttributeAssignments().forEach(assignment -> {
			assignAttribute(assignment, match, parameters);
		});
		return Optional.of(match);
	}

	private static void assignAttribute(final IBeXAttributeAssignment assignment, final IMatch match,
			final Map<String, Object> parameters) {
		EObject object = (EObject) match.get(assignment.getNode().getName());
		EAttribute attribute = assignment.getType();
		IBeXAttributeValue value = assignment.getValue();
		if (value instanceof IBeXConstant) {
			object.eSet(attribute, ((IBeXConstant) value).getValue());
		}
		if (value instanceof IBeXEnumLiteral) {
			EEnumLiteral enumLiteral = ((IBeXEnumLiteral) value).getLiteral();
			// Need to get actual Java instance. Cannot pass EnumLiteral here!
			Enumerator instance = enumLiteral.getInstance();
			if (instance == null) {
				throw new IllegalArgumentException("Missing object for " + enumLiteral);
			}
			object.eSet(attribute, instance);
		}
		if (value instanceof IBeXAttributeParameter) {
			String parameterName = ((IBeXAttributeParameter) value).getName();
			if (!parameters.containsKey(parameterName)) {
				throw new IllegalArgumentException("Missing required parameter " + parameterName);
			}
			object.eSet(attribute, parameters.get(parameterName));
		}
	}
}
