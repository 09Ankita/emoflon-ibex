package org.emoflon.ibex.tgg.operational.defaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.ibex.common.emf.EMFEdge;
import org.emoflon.ibex.common.emf.EMFManipulationUtils;
import org.emoflon.ibex.tgg.compiler.patterns.common.IbexBasePattern;
import org.emoflon.ibex.tgg.operational.IGreenInterpreter;
import org.emoflon.ibex.tgg.operational.csp.IRuntimeTGGAttrConstrContainer;
import org.emoflon.ibex.tgg.operational.matches.IMatch;
import org.emoflon.ibex.tgg.operational.patterns.IGreenPattern;
import org.emoflon.ibex.tgg.operational.strategies.OperationalStrategy;
import org.emoflon.ibex.tgg.util.String2EPrimitive;

import language.TGGAttributeConstraintOperators;
import language.TGGAttributeExpression;
import language.TGGEnumExpression;
import language.TGGInplaceAttributeExpression;
import language.TGGLiteralExpression;
import language.TGGRuleCorr;
import language.TGGRuleEdge;
import language.TGGRuleNode;
import runtime.RuntimePackage;

/**
 * @author leblebici Util class for creating EObjects, Edges, and
 *         Correspondences for a given set of green TGGRuleElement
 */
public class IbexGreenInterpreter implements IGreenInterpreter {
	private static final Logger logger = Logger.getLogger(IbexGreenInterpreter.class);
	
	
	private int numOfCreatedNodes = 0;
	private int numOfCreatedEdges= 0;
	private OperationalStrategy operationalStrategy;

	public IbexGreenInterpreter(OperationalStrategy operationalStrategy) {
		this.operationalStrategy = operationalStrategy;
	}

	public void createNonCorrNodes(IMatch comatch, Collection<TGGRuleNode> greenNodes, Resource nodeResource) {
		for (TGGRuleNode n : greenNodes)
			comatch.put(n.getName(), createNode(comatch, n, nodeResource));
	}

	public Collection<EMFEdge> createEdges(IMatch comatch, Collection<TGGRuleEdge> greenEdges, boolean createEMFEdge) {
		numOfCreatedEdges += greenEdges.stream().filter(e -> !e.getSrcNode().getType().equals(RuntimePackage.eINSTANCE.getTGGRuleApplication())).count();
		Collection<EMFEdge> result = new ArrayList<>();
		for (TGGRuleEdge e : greenEdges) {
			EObject src = (EObject) comatch.get(e.getSrcNode().getName());
			EObject trg = (EObject) comatch.get(e.getTrgNode().getName());
			if (createEMFEdge) {
				EMFManipulationUtils.createEdge(src, trg, e.getType());
			}
			result.add(new EMFEdge(src, trg, e.getType()));
		}

		comatch.getCreatedEdges().addAll(result);

		return result;
	}

	public void createCorrs(IMatch comatch, Collection<TGGRuleCorr> greenCorrs, Resource corrR) {
		for (TGGRuleCorr c : greenCorrs) {
			comatch.put(c.getName(), createCorr(comatch, c, comatch.get(c.getSource().getName()),
					comatch.get(c.getTarget().getName()), corrR));
		}
	}

	private EObject createNode(IMatch match, TGGRuleNode node, Resource resource) {
		numOfCreatedNodes++;
		
		EObject newObj = EcoreUtil.create(node.getType());
		handlePlacementInResource(node, resource, newObj);

		applyInPlaceAttributeAssignments(match, node, newObj);
		applyAttributeAssignments(match, node, newObj);

		return newObj;
	}

	private void applyAttributeAssignments(IMatch match, TGGRuleNode node, EObject newObj) {
		Collection<String> attributeNames = match.getParameterNames().stream() //
				.filter(pname -> {
					Optional<Pair<String, String>> o = IbexBasePattern.getNodeAndAttrFromVarName(pname);
					Optional<Boolean> check = o.map(node_attr -> node_attr.getLeft().equals(node.getName()));
					return check.orElse(false);
				}).collect(Collectors.toList());

		for (String node_attr : attributeNames) {
			Object attributeValue = match.get(node_attr);
			Pair<String, String> node_attr_pair = IbexBasePattern.getNodeAndAttrFromVarName(node_attr)
					.orElseThrow(() -> new IllegalStateException("Missing attribute value"));
			String attributeName = node_attr_pair.getRight();

			EStructuralFeature feature = node.getType().getEStructuralFeature(attributeName);
			newObj.eSet(feature, attributeValue);
		}
	}

	private void applyInPlaceAttributeAssignments(IMatch match, TGGRuleNode node, EObject newObj) {
		for (TGGInplaceAttributeExpression attrExpr : node.getAttrExpr()) {
			if (attrExpr.getOperator().equals(TGGAttributeConstraintOperators.EQUAL)) {
				if (attrExpr.getValueExpr() instanceof TGGLiteralExpression) {
					TGGLiteralExpression tle = (TGGLiteralExpression) attrExpr.getValueExpr();
					newObj.eSet(attrExpr.getAttribute(),
							String2EPrimitive.convertLiteral(tle.getValue(), attrExpr.getAttribute().getEAttributeType()));
					continue;
				}
				if (attrExpr.getValueExpr() instanceof TGGEnumExpression) {
					TGGEnumExpression tee = (TGGEnumExpression) attrExpr.getValueExpr();
					newObj.eSet(attrExpr.getAttribute(), tee.getLiteral().getInstance());
					continue;
				}
				if (attrExpr.getValueExpr() instanceof TGGAttributeExpression) {
					TGGAttributeExpression tae = (TGGAttributeExpression) attrExpr.getValueExpr();
					EObject obj = (EObject) match.get(tae.getObjectVar().getName());
					newObj.eSet(attrExpr.getAttribute(), obj.eGet(tae.getAttribute()));
					continue;
				}

			}
		}
	}

	private void handlePlacementInResource(TGGRuleNode node, Resource resource, EObject newObj) {
		resource.getContents().add(newObj);
	}

	private EObject createCorr(IMatch comatch, TGGRuleNode node, Object src, Object trg, Resource corrR) {
		numOfCreatedNodes++;
		
		EObject corr = createNode(comatch, node, corrR);
		corr.eSet(corr.eClass().getEStructuralFeature("source"), src);
		corr.eSet(corr.eClass().getEStructuralFeature("target"), trg);
		return corr;
	}

	@Override
	public Optional<IMatch> apply(IGreenPattern greenPattern, String ruleName, IMatch match) {
		// Check if match is valid
		if (matchIsInvalid(ruleName, greenPattern, match)) {
			logger.debug("Blocking application as match is invalid.");
			return Optional.empty();
		}

		// Check if pattern should be ignored
		if (greenPattern.isToBeIgnored(match)) {
			logger.debug("Blocking application as match is to be ignored.");
			return Optional.empty();
		}

		// Check if all attribute values provided match are as expected
		IRuntimeTGGAttrConstrContainer cspContainer = greenPattern.getAttributeConstraintContainer(match);
		if (!cspContainer.solve()) {
			logger.debug("Blocking application as attribute conditions don't hold.");
			return Optional.empty();
		}

		IMatch comatch = match.copy();

		createNonCorrNodes(comatch, greenPattern.getSrcNodes(), operationalStrategy.getSourceResource());
		createEdges(comatch, greenPattern.getSrcEdges(), true);

		createNonCorrNodes(comatch, greenPattern.getTrgNodes(), operationalStrategy.getTargetResource());
		createEdges(comatch, greenPattern.getTrgEdges(), true);

		cspContainer.applyCSPValues(comatch);

		createCorrs(comatch, greenPattern.getCorrNodes(), operationalStrategy.getCorrResource());

		return Optional.of(comatch);
	}

	private boolean matchIsInvalid(String ruleName, IGreenPattern greenPattern, IMatch match) {
		return someElementsAlreadyProcessed(ruleName, greenPattern, match)
				|| !conformTypesOfGreenNodes(match, greenPattern, ruleName)
				|| !allContextElementsAlreadyProcessed(match, greenPattern, ruleName);
	}

	protected boolean someElementsAlreadyProcessed(String ruleName, IGreenPattern greenPattern, IMatch match) {
		return operationalStrategy.someEdgesAlreadyProcessed(greenPattern.getEdgesMarkedByPattern(), match);
	}

	protected boolean conformTypesOfGreenNodes(IMatch match, IGreenPattern greenPattern, String ruleName) {
		for (TGGRuleNode gsn : greenPattern.getNodesMarkedByPattern()) {
			if (gsn.getType() != ((EObject) match.get(gsn.getName())).eClass())
				return false;
		}

		return true;
	}

	protected boolean allContextElementsAlreadyProcessed(IMatch match, IGreenPattern greenPattern, String ruleName) {
		return operationalStrategy.allEdgesAlreadyProcessed(greenPattern.getMarkedContextEdges(), match);
	}

	@Override
	public int getNumOfCreatedElements() {
		return numOfCreatedNodes + numOfCreatedEdges;
	}
}
