/*
 * Created on 2005-01-30
 *
 */
package seg.jUCMNav.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import seg.jUCMNav.model.ucm.Component;
import seg.jUCMNav.model.ucm.Link;
import seg.jUCMNav.model.ucm.Node;
import seg.jUCMNav.model.ucm.UcmDiagram;

/**
 * @author Etienne Tremblay
 *
 */
public class GraphicalEditPartFactory implements EditPartFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof UcmDiagram)
			return new UcmDiagramEditPart((UcmDiagram)model);
		else if(model instanceof Link)
			return new LinkEditPart((Link)model);
		else if(model instanceof Component)
			return new ComponentEditPart((Component)model);
		else if(model instanceof Node)
			return new UcmNodeEditPart((Node)model);
		else
			return null;
	}

}
