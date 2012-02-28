package seg.jUCMNav.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import seg.jUCMNav.Messages;
import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.metadata.ChangeMetadataCommand;
import seg.jUCMNav.model.util.ArrayAndListUtils;
import urn.URNspec;
import urncore.Metadata;

/**
 * Adds stereotype definitions to a URN spec.
 * 
 * @author amiga
 */

//name="StereotypeDef", value="ST_CLASSTYPE,CLASS1,IntentionalElement"
//name="StereotypeDef", value="ST_CLASSTYPE,CLASS2,IntentionalElement"
//name="StereotypeDef", value="ST_CLASSTYPE,OTHER,IntentionalElement"
//name="StereotypeDef", value="acceptStereotype,CLASS1,EvaluationStrategy"
//name="StereotypeDef", value="acceptStereotype,CLASS2,EvaluationStrategy"
//name="StereotypeDef", value="acceptStereotype,OTHER,EvaluationStrategy"


public class AddStereotypeDefinitionsAction extends URNSelectionAction {

    public static final String ADD_STEREOTYPE_DEFINITIONS = "seg.jUCMNav.AddStereotypeDefinitions"; //$NON-NLS-1$

    private String [] values = { "ST_CLASSTYPE,CLASS1,IntentionalElement", "ST_CLASSTYPE,CLASS2,IntentionalElement", "ST_CLASSTYPE,OTHER,IntentionalElement",
    		"acceptStereotype,CLASS1,EvaluationStrategy", "acceptStereotype,CLASS2,EvaluationStrategy", "acceptStereotype,OTHER,EvaluationStrategy"
    };

	public AddStereotypeDefinitionsAction(IWorkbenchPart part) {
		super(part);
        setId(ADD_STEREOTYPE_DEFINITIONS);
	}

    /**
     * If you have a URNspec selected.
     */
    protected boolean calculateEnabled() {
        SelectionHelper sel = new SelectionHelper(getSelectedObjects());
        URNspec urnspec = sel.getUrnspec();

        if( urnspec == null )
        	return false;
        else
        	return true;
    }

     protected Command getCommand()
     {
 		String title, message, commandLabel;
 		Metadata [] mdList, sdList;
 		
		final int CANCEL = 0;
		final int UPDATE = 1;
		int userChoice;
		
        SelectionHelper sel = new SelectionHelper(getSelectedObjects());
        URNspec urnspec = sel.getUrnspec();
        
        if( urnspec == null )
        	return null;
        
        if( this.hasStereotypeDefinitions(urnspec)) { // inform user of existing definitions and provide option to cancel 
        
            // remove previous definitions by making a list of all other URNspec metadata
            Vector<Metadata> otherMetadata = new Vector<Metadata>();
            Vector<Metadata> existingMetadata = new Vector<Metadata>();
            
        	if( urnspec.getMetadata().size() > 0 ) {
        		for( Iterator iter = urnspec.getMetadata().iterator(); iter.hasNext();) {
        			Metadata md = (Metadata) iter.next();	
        			if( md.getName().equalsIgnoreCase( "StereotypeDef" )){ //$NON-NLS-1$
        				existingMetadata.add(md);
        			} else {
        				otherMetadata.add(md);        				
        			}
        		}    		
        	}
        	
			StringBuilder messageBuf = new StringBuilder();
			messageBuf.append( "A set of stereotype definitions already exists in the current model.\n\n" );
        
			for( Metadata em : existingMetadata ) {
				messageBuf.append( "\t" + em.getValue() + "\n" );
			}
			
			messageBuf.append( "\nDo you wish to update these definitions ?");
			
			title = "Stereotype Definitions Exist";
			message = messageBuf.toString();
			
			final String[] labels = { "Cancel", "Update Stereotype Definitions" };

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog md = new MessageDialog( shell, title, null, message, MessageDialog.QUESTION, labels, UPDATE );
			md.create();
			userChoice = md.open();

			if( userChoice == CANCEL) {
				return null;
			}

            mdList = (Metadata[]) otherMetadata.toArray(new Metadata[0]);
        	commandLabel = Messages.getString("ActionRegistryManager.updateStereotypeDefinitions");            
        } else {
        	mdList = (Metadata[]) urnspec.getMetadata().toArray(new Metadata[0]);
        	commandLabel = Messages.getString("ActionRegistryManager.addStereotypeDefinitions");
        }
        
        sdList = this.getStereotypeDefinitions(urnspec);        
        
        Metadata[] combinedList = ArrayAndListUtils.concatenateArrays( mdList, sdList );
        
        Command cmd = new ChangeMetadataCommand( urnspec, combinedList, commandLabel );
        
        return cmd;
    }
    
    protected boolean hasStereotypeDefinitions( URNspec urnspec )
    {	
    	if( urnspec.getMetadata().size() > 0 ) {
    		for( Iterator iter = urnspec.getMetadata().iterator(); iter.hasNext();) {
    			Metadata md = (Metadata) iter.next();	
    			if(md.getName().equalsIgnoreCase( "StereotypeDef" )){ //$NON-NLS-1$
    					return true;
    			}
    		}    		
    	}
    	
    	return false;
    }
    
    protected Metadata [] getStereotypeDefinitions( URNspec urnspec )
    {    		
    	// populate new list with stereotypes
        Metadata [] mdList = new Metadata[values.length];
        Metadata newMetadata;
        for( int i = 0; i < values.length; i++ ) {
        	newMetadata = (Metadata) ModelCreationFactory.getNewObject(urnspec, Metadata.class);
        	newMetadata.setName("StereotypeDef");
        	newMetadata.setValue(values[i]);
        	mdList[i] = newMetadata;
        }
        
        return mdList;
    }
}