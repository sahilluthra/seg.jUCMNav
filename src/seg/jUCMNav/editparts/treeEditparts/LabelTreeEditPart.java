package seg.jUCMNav.editparts.treeEditparts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.views.properties.IPropertySource;

import urn.URNspec;


/**
 * Created 2005-05-17
 * 
 * @author Etienne Tremblay
 */
public class LabelTreeEditPart extends UcmModelElementTreeEditPart {
	
	private URNspec root;

	/**
	 * @param model
	 */
	public LabelTreeEditPart(Object model, URNspec root) {
		super(model);
		this.root = root;
	}

	
	public void activate() {
		if (!isActive())
            ((EObject) root.getUrndef()).eAdapters().add(this);
	}
	public void deactivate() {
		 if (isActive())
            ((EObject) root.getUrndef()).eAdapters().remove(this);
	}
	
	protected IPropertySource getPropertySource() {return null;}
	
	protected List getModelChildren() {
		ArrayList list = new ArrayList();
		if(getLabel().equals("Components"))
			list.addAll(root.getUrndef().getComponents());
		else if(getLabel().equals("Responsibilities"))
			list.addAll(root.getUrndef().getResponsibilities());
		return list;
	}
	
	protected String getLabel(){
		return (String)getModel();
	}
	
	protected String getText() {
		return getLabel();
	}
}