/**
 * 
 */
package seg.jUCMNav.editparts;

import grl.GRLGraph;
import grl.IntentionalElementRef;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import urncore.Label;

/**
 * @author Jean-Fran�ois Roy
 *
 */
public class GrlGraphicalEditPartFactory implements EditPartFactory {

    private GRLGraph graph;
    
    public GrlGraphicalEditPartFactory(){
        super();
    }
    
    public GrlGraphicalEditPartFactory(GRLGraph graph){
        this.graph = graph;
    }
    
    /* 
     * Create new instances of edit part for the model elements
     * 
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof GRLGraph){
            return new GrlGraphEditPart((GRLGraph)model);
        }
        else if(model instanceof IntentionalElementRef){
            return new GrlNodeEditPart((IntentionalElementRef)model, graph);
        }
        else if(model instanceof Label){
            return new LabelEditPart((Label)model);
        }
        else {  
            System.out.println("Unknown class in GrlGraphicalEditPartFactory.createEditPart();"); //$NON-NLS-1$
            assert false : "Unknown class in GrlGraphicalEditPartFactory.createEditPart();"; //$NON-NLS-1$
            return null;
        }
    }

}