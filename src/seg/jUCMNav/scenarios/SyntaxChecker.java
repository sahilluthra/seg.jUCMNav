package seg.jUCMNav.scenarios;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import seg.jUCMNav.editors.UCMNavMultiPageEditor;
import seg.jUCMNav.model.util.URNNamingHelper;
import seg.jUCMNav.scenarios.model.TraversalWarning;
import seg.jUCMNav.scenarios.parser.SimpleNode;
import ucm.map.EndPoint;
import ucm.map.NodeConnection;
import ucm.map.OrFork;
import ucm.map.PathNode;
import ucm.map.PluginBinding;
import ucm.map.StartPoint;
import ucm.map.UCMmap;
import ucm.map.WaitingPlace;
import ucm.scenario.ScenarioDef;
import ucm.scenario.ScenarioGroup;
import urn.URNspec;
import urncore.Condition;
import urncore.IURNDiagram;
import urncore.Responsibility;
import urncore.URNmodelElement;

/**
 * Verifies the syntax of all conditions / responsibilities. Also manages refreshing the problems view. 
 * @author jkealey
 *
 */
public class SyntaxChecker {

	private static void verifyCondition(URNspec urn, Vector errors, EObject location, String expr) {
		Object o = ScenarioUtils.parse(expr, ScenarioUtils.getEnvironment(urn), false);
		if (!(o instanceof SimpleNode))
		{
			errors.add(new TraversalWarning((String)o, location, IMarker.SEVERITY_ERROR));
		}
	}

	private static void verifyMapConditions(URNspec urn, Vector errors) {
		for (Iterator iter = urn.getUrndef().getSpecDiagrams().iterator(); iter.hasNext();) {
			IURNDiagram diag = (IURNDiagram) iter.next();
			if (diag instanceof UCMmap) {
				for (Iterator iterator = ((UCMmap)diag).getNodes().iterator(); iterator.hasNext();) {
					PathNode node = (PathNode) iterator.next();
					if (node instanceof StartPoint) {
						if (((StartPoint)node).getPrecondition()!=null) {
							verifyCondition(urn, errors, node, ((StartPoint)node).getPrecondition().getExpression());
						}
					} else if (node instanceof EndPoint) {
						if (((EndPoint)node).getPostcondition()!=null) {
							verifyCondition(urn, errors, node, ((EndPoint)node).getPostcondition().getExpression());
						}
					} else if (node instanceof OrFork || node instanceof WaitingPlace) {
						for (Iterator it2 = node.getSucc().iterator(); it2.hasNext();) {
							NodeConnection nc = (NodeConnection) it2.next();
							if (nc.getCondition()!=null)
							{
								verifyCondition(urn, errors, node,nc.getCondition().getExpression());								
							}
						}
					}
				}
			}
		}		
	}

	private static void verifyPluginBindingSyntax(URNspec urn, Vector errors) {
		for (Iterator iter = urn.getUrndef().getSpecDiagrams().iterator(); iter.hasNext();) {
			IURNDiagram diag = (IURNDiagram) iter.next();
			if (diag instanceof UCMmap) {
				for (Iterator iterator = ((UCMmap)diag).getParentStub().iterator(); iterator.hasNext();) {
					PluginBinding binding = (PluginBinding) iterator.next();
					if (binding.getPrecondition()!=null) {
						String expr=binding.getPrecondition().getExpression();
						verifyCondition(urn, errors,  binding.getStub(), expr);
					}
				}
			}
		}
	}

	private static void verifyResponsibilitySyntax(URNspec urn, Vector errors) {
		for (Iterator iter = urn.getUrndef().getResponsibilities().iterator(); iter.hasNext();) {
			Responsibility resp = (Responsibility) iter.next();
			if (!ScenarioUtils.isEmptyResponsibility(resp)) {
				Object o = ScenarioUtils.parse(resp.getExpression(), ScenarioUtils.getEnvironment(resp), true);
				if (!(o instanceof SimpleNode))
				{
					if (resp.getRespRefs().size()>0)
						errors.add(new TraversalWarning((String)o, (EObject)resp.getRespRefs().get(0), IMarker.SEVERITY_ERROR));
					else
						errors.add(new TraversalWarning((String)o, resp, IMarker.SEVERITY_ERROR));
				}
			}
		}
	}	
	
	private static void verifyScenarioPrePostConditions(URNspec urn, Vector errors) {
		for (Iterator iter = urn.getUcmspec().getScenarioGroups().iterator(); iter.hasNext();) {
			ScenarioGroup group = (ScenarioGroup) iter.next();
			for (Iterator iterator = group.getScenarios().iterator(); iterator.hasNext();) {
				ScenarioDef scenario = (ScenarioDef) iterator.next();
				for (Iterator it2 = scenario.getPreconditions().iterator(); it2.hasNext();) {
					Condition cond = (Condition) it2.next();
					verifyCondition(urn, errors, scenario, cond.getExpression());
				}
				for (Iterator it2 = scenario.getPostconditions().iterator(); it2.hasNext();) {
					Condition cond = (Condition) it2.next();
					verifyCondition(urn, errors, scenario, cond.getExpression());
				}
				
			}
			
		}
		
	}

	/**
	 * Returns a vector of TraversalWarnings for all the elements that do not have a valid syntax. 
	 * 
	 * @param urn the urnspec to be analyzed
	 * @return vector of TraversalWarnings for all the elements that do not have a valid syntax.
	 */
	public static Vector verifySyntax(URNspec urn) {
		Vector errors = new Vector();
		verifyResponsibilitySyntax(urn,errors);
		verifyPluginBindingSyntax(urn,errors);
		verifyMapConditions(urn,errors);
		verifyScenarioPrePostConditions(urn,errors);
		return errors;
		
	}		
	
	/**
	 * Clears the warnings associated to this file and replaces them with those supplied in the vector. 
	 * 
	 * @param warnings
	 */
	public static void refreshProblemsView(Vector warnings) {
		UCMNavMultiPageEditor editor = (UCMNavMultiPageEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFile resource = ((FileEditorInput) editor.getEditorInput()).getFile();
		try {

			IMarker[] existingMarkers = resource.findMarkers(IMarker.PROBLEM, true, 3);
			for (int i = 0; i < existingMarkers.length; i++) {
				IMarker marker = existingMarkers[i];
				marker.delete();
			}
		} catch (CoreException ex) {
			System.out.println(ex);
		}
		
		if (warnings.size() > 0) {

			
			for (Iterator iter = warnings.iterator(); iter.hasNext();) {
				TraversalWarning o = (TraversalWarning) iter.next();
				
				try {
					IMarker marker = resource.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.SEVERITY, o.getSeverity());
					marker.setAttribute(IMarker.MESSAGE, o.toString());
					if (o.getLocation() instanceof URNmodelElement) {
						URNmodelElement elem = (URNmodelElement) o.getLocation();
						marker.setAttribute(IMarker.LOCATION, URNNamingHelper.getName(elem));
						marker.setAttribute("EObject", ((URNmodelElement)o.getLocation()).getId()); //$NON-NLS-1$
					} else if (o.getLocation()!=null) {
						marker.setAttribute(IMarker.LOCATION, o.getLocation().toString());
					}
					
					if (o.getCondition()!=null && o.getCondition().eContainer()!=null)
					{
						if (o.getCondition().eContainer() instanceof StartPoint) {
							StartPoint start = (StartPoint)o.getCondition().eContainer();
							marker.setAttribute("NodePreCondition", start.getId() ); //$NON-NLS-1$
						} else if (o.getCondition().eContainer() instanceof EndPoint) {
							EndPoint end = (EndPoint)o.getCondition().eContainer();
							marker.setAttribute("NodePostCondition", end.getId() ); //$NON-NLS-1$
						} else if (o.getCondition().eContainer() instanceof NodeConnection) {
							NodeConnection ncx = (NodeConnection)o.getCondition().eContainer();
							PathNode pn = (PathNode) ncx.getSource();
							marker.setAttribute("Condition", pn.getId() ); //$NON-NLS-1$
							for (int i=0;i<pn.getSucc().size();i++) {
								NodeConnection nc = (NodeConnection) pn.getSucc().get(i);
								if (nc.getCondition() == o.getCondition()) {
									marker.setAttribute("ConditionIndex", i ); //$NON-NLS-1$
								}
							}
						}else if (o.getCondition().eContainer() instanceof ScenarioDef) {
							ScenarioDef scenario = (ScenarioDef)o.getCondition().eContainer();
							marker.setAttribute("Scenario", scenario.getId() ); //$NON-NLS-1$
							marker.setAttribute("ScenarioPreConditionIndex", scenario.getPreconditions().indexOf(o.getCondition())); //$NON-NLS-1$
							marker.setAttribute("ScenarioPostConditionIndex", scenario.getPostconditions().indexOf(o.getCondition())); //$NON-NLS-1$
						}
					}
					else  if (o.getLocation() instanceof OrFork || o.getLocation() instanceof WaitingPlace ) {
							PathNode pn = (PathNode)o.getLocation();
							marker.setAttribute("Condition", pn.getId() ); //$NON-NLS-1$							
					}
					resource.findMarkers("seg.jUCMNav.WarningMarker", true, 1); //$NON-NLS-1$
				} catch(CoreException ex) 
				{
					//System.out.println(ex);
				}
				
			}
//			throw new TraversalException(b.toString());

		}		
	}
	
}