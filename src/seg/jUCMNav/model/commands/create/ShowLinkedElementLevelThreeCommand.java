package seg.jUCMNav.model.commands.create;

import grl.ElementLink;
import grl.GRLGraph;
import grl.GRLLinkableElement;
import grl.IntentionalElement;
import grl.IntentionalElementRef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;

import seg.jUCMNav.Messages;
import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.JUCMNavCommand;
import seg.jUCMNav.model.commands.delete.DeleteAllLinkRefCommand;
import seg.jUCMNav.model.commands.delete.internal.DisconnectGRLNodeCommand;
import seg.jUCMNav.model.commands.delete.internal.RemoveURNmodelElementCommand;
import urn.URNspec;
import urncore.GRLmodelElement;
import urncore.IURNDiagram;

/**
 * @author rouzbahan
 *
 */
public class ShowLinkedElementLevelThreeCommand extends Command implements JUCMNavCommand
{
    boolean levelTwoSrc = false, levelTwoDest = false;
    private URNspec urnspec;
    private GRLmodelElement grlelem;
    private GRLGraph grlGraph;
    private IntentionalElementRef objRef;
    private IURNDiagram diagramOfElement;

    private IntentionalElement chosenIntentionalElement; // Selected IntentionalElement
    
    private List<IntentionalElement> IntentionalElementList; // List of the IntentionalElements in the grl Graph   

    private List<IntentionalElementRef> currentIntentionalElementRefList; // List of the refs of IntentionalElements in the grl Graph
    private List<IntentionalElementRef> addedIntentionalElementRefList; // List of the added IntentionalElements refs into the grlGraph
    private List<IntentionalElementRef> LevelTwoAddedIntentionalElementRefList; // List of the added IntentionalElements refs into the grlGraph
    private List<IntentionalElementRef> finalIntentionalElementRefList; // List of all the IntentionalElements refs in the grl Graph
    private List<IntentionalElementRef> levelTwoFinalIntentionalElementRefList;

    private List<GRLLinkableElement> srcGRLLinkableElementList; // List of the GRLLinkable(IntentionalElement) Elements that the selected element is the source for it
    private List<GRLLinkableElement> destGRLLinkableElementList; // List of the GRLLinkable(IntentionalElement) Elements that the selected element is the destination for it
    private List<GRLLinkableElement> levelTwoSrcGRLLinkableElementList;
    private List<GRLLinkableElement> levelTwoDestGRLLinkableElementList;
    
    private ArrayList<IntentionalElement> LogicallyConnectedElementList;
    private ArrayList<IntentionalElement> inGraphLogicalElementList;
    private ArrayList<IntentionalElement> missingConnectedIntentionalElementList;
    
    private List<ElementLink> linksSourceList;
    private List<ElementLink> linksDestinationList;
    private List<ElementLink> levelTwolinksSourceSrcGRLLinkableElementList;
    private List<ElementLink> levelTwolinksDestinationSrcGRLLinkableElementList;
    private List<ElementLink> levelTwolinksSourceDestGRLLinkableElementList;  
    private List<ElementLink> levelTwolinksDestinationDestGRLLinkableElementList;
    
    public ShowLinkedElementLevelThreeCommand(URNspec spec, EObject obj, IntentionalElementRef ref) 
    {
        urnspec = spec;
        objRef = ref;
        diagramOfElement = objRef.getDiagram();
        grlGraph = (GRLGraph) objRef.getDiagram();
                
        if (obj instanceof GRLLinkableElement) 
        {
            if (obj instanceof IntentionalElement)
            {  
                grlelem = (GRLLinkableElement) obj;
                IntentionalElementList = new ArrayList<IntentionalElement>(urnspec.getGrlspec().getIntElements());
                currentIntentionalElementRefList = new ArrayList<IntentionalElementRef>(grlGraph.getNodes());
                
                for (IntentionalElement IE : IntentionalElementList)
                    if (IE.equals(grlelem))
                        chosenIntentionalElement = (IntentionalElement) IE;                   
            }                                                              
        }
        
        setLabel(Messages.getString("ActionRegistryManager.ShowLinkedElementLevelThree")); //$NON-NLS-1$
    }
    
    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() 
    {
        boolean exacutable = false;
        
        if ( urnspec != null && grlelem != null && chosenIntentionalElement != null )
            exacutable = true;        
        
        return exacutable;
    }
    
    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() 
    {
        boolean found = false; 
        int levelTwoCounter = 0;
        ArrayList <IntentionalElementRef> tempIEList;
        IntentionalElement tempIE;
        GRLLinkableElement tempGRLElement;
        
        srcGRLLinkableElementList = new ArrayList<GRLLinkableElement>();
        destGRLLinkableElementList = new ArrayList<GRLLinkableElement>();
        LogicallyConnectedElementList = new ArrayList<IntentionalElement>();
        
        // Initializing ElementLink lists to find related intentional elements
        linksSourceList = new ArrayList<ElementLink>(chosenIntentionalElement.getLinksSrc());
        linksDestinationList = new ArrayList<ElementLink>(chosenIntentionalElement.getLinksDest());
        
        for (ElementLink EL : linksSourceList)
            LogicallyConnectedElementList.add((IntentionalElement)EL.getDest());
                
        for (ElementLink EL : linksDestinationList)
            LogicallyConnectedElementList.add((IntentionalElement)EL.getSrc());
        
        /* carry a bug
        levelTwoSrcGRLLinkableElementList = new ArrayList<GRLLinkableElement>();
        levelTwoDestGRLLinkableElementList = new ArrayList<GRLLinkableElement>();
        
        // Initializing ElementLink lists to find related intentional elements
        linksSourceList = new ArrayList<ElementLink>(chosenIntentionalElement.getLinksSrc());
        linksDestinationList = new ArrayList<ElementLink>(chosenIntentionalElement.getLinksDest());
                
        // Creating list of intentional elements that the element is the source of them and its ref is not in the refs list of the graph
        for (ElementLink EL : linksSourceList)            
        {
            tempIE = (IntentionalElement) EL.getDest();
            tempIEList = new ArrayList <IntentionalElementRef>( tempIE.getRefs() );
            
            for ( int i = 0; i < tempIEList.size(); i++ )
            {
                if (currentIntentionalElementRefList.contains(tempIEList.get( i )))
                {    
                    found = true;
                    levelTwoCounter++;
                }
            }
            
            if ( !found )
                srcGRLLinkableElementList.add( (IntentionalElement) EL.getDest());            
            
            levelTwoSrcGRLLinkableElementList.add( (IntentionalElement) EL.getDest());
            found = false;
        }
        
        if (levelTwoCounter == linksSourceList.size())
        {
            levelTwoSrc = true;
            System.out.println("\nlevelTwoSrc is : " + levelTwoSrc);
        }
        
        levelTwoCounter = 0;
        
        // Creating list of intentional elements that the element is the destination of them and its ref is not in the refs list of the graph
        for (ElementLink EL : linksDestinationList)
        {
            tempIE = (IntentionalElement) EL.getSrc();
            tempIEList = new ArrayList <IntentionalElementRef>( tempIE.getRefs() );
          
            for (int i = 0; i < tempIEList.size(); i++)
            {
                if (currentIntentionalElementRefList.contains(tempIEList.get( i )))
                {
                    found = true;
                    levelTwoCounter++;
                }
            } 
          
            if ( !found )   
                destGRLLinkableElementList.add( (IntentionalElement) EL.getSrc());
            
            levelTwoDestGRLLinkableElementList.add( (IntentionalElement) EL.getSrc());
            found = false;
        }
        
        if (levelTwoCounter == linksDestinationList.size())
        {
            levelTwoDest = true;
            System.out.println("\nlevelTwoDest is : " + levelTwoDest);
        }
        */        
        redo();   
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() 
    {
        testPreConditions();
        
        IntentionalElementRef ieRef = null;
        
        // Finding all refs in the graph at the begining of any redo
        currentIntentionalElementRefList = new ArrayList<IntentionalElementRef>(grlGraph.getNodes());
        
        // Finding the definition of all the refs in the graph
        inGraphLogicalElementList = new ArrayList<IntentionalElement>();
        for (IntentionalElementRef IER : currentIntentionalElementRefList)
            inGraphLogicalElementList.add(IER.getDef());
        
        // Removing the selected node from the inGraphLogicalElementList
        inGraphLogicalElementList.remove(chosenIntentionalElement);
        
        // Finding and adding missing connected Intentional Elements to the list below
        missingConnectedIntentionalElementList = new ArrayList<IntentionalElement>();
        for (IntentionalElement IE : LogicallyConnectedElementList)
            if (!inGraphLogicalElementList.contains(IE))
                missingConnectedIntentionalElementList.add(IE);
        
        // Creating refs for the missingConnectedInentionalElementList and adding them to graph and also to the list of addedIntentionalElementRefList
        addedIntentionalElementRefList = new ArrayList<IntentionalElementRef>();
        for (IntentionalElement IE : missingConnectedIntentionalElementList)
        {
            ieRef = (IntentionalElementRef) ModelCreationFactory.getNewObject(urnspec, IntentionalElementRef.class);
            ieRef.setDef( (IntentionalElement) IE);
            addedIntentionalElementRefList.add(ieRef);
            AddIntentionalElementRefCommand cmd = new AddIntentionalElementRefCommand(grlGraph, ieRef);
            cmd.execute();
        }
        
        // Adding all the missing links between all the nodes and all the added nodes in the graph
        for (IntentionalElementRef IER : addedIntentionalElementRefList)
        {
            CreateAllLinkRefCommand cmd = new CreateAllLinkRefCommand(IER);
            cmd.execute();
        }
        
        // Calling ShowLinkedElementCommand for all the logically connected elements
        for (IntentionalElement IE : LogicallyConnectedElementList)
        {   
            List<IntentionalElementRef> tempIERList = IE.getRefs();
            for(IntentionalElementRef IER : tempIERList) // Finding the ref of the connected element that is located in the graph 
                if (IER.getDiagram().equals(diagramOfElement))
                {
                    ieRef = IER;
                    break;
                }
            ShowLinkedElementLevelTwoCommand cmd = new ShowLinkedElementLevelTwoCommand(urnspec, IE, ieRef);
            cmd.execute();
        }
        
        
        /* carry a bug
        addedIntentionalElementRefList = new ArrayList<IntentionalElementRef>();
        finalIntentionalElementRefList = new ArrayList<IntentionalElementRef>();
        
        if (levelTwoSrc == false)
        {
            for (int i = 0; i < srcGRLLinkableElementList.size(); i++)
            {
                ieRef = (IntentionalElementRef) ModelCreationFactory.getNewObject(urnspec, IntentionalElementRef.class);
                ieRef.setDef( (IntentionalElement) srcGRLLinkableElementList.get(i));
                
                if (currentIntentionalElementRefList.contains(ieRef))
                {
                    finalIntentionalElementRefList.add(currentIntentionalElementRefList.get(currentIntentionalElementRefList.indexOf(ieRef)));
                    continue;
                }
                else
                {
                    addedIntentionalElementRefList.add(ieRef);
                    finalIntentionalElementRefList.add(ieRef);
                    AddIntentionalElementRefCommand comm1 = new AddIntentionalElementRefCommand(grlGraph, ieRef);
                    comm1.execute();
                }
            }
        }
        
        if (levelTwoDest == false)
        {
            for (int i = 0; i < destGRLLinkableElementList.size(); i++)
            {
                ieRef = (IntentionalElementRef) ModelCreationFactory.getNewObject(urnspec, IntentionalElementRef.class);
                ieRef.setDef( (IntentionalElement) destGRLLinkableElementList.get(i));            
               
                if (currentIntentionalElementRefList.contains(ieRef))
                {
                    finalIntentionalElementRefList.add(currentIntentionalElementRefList.get(currentIntentionalElementRefList.indexOf(ieRef)));
                    continue;
                }
                else
                {
                    addedIntentionalElementRefList.add(ieRef);
                    finalIntentionalElementRefList.add(ieRef);
                    AddIntentionalElementRefCommand comm1 = new AddIntentionalElementRefCommand(grlGraph, ieRef);
                    comm1.execute();
                }
            }
        }
        
        // Adding all the refs of links between all the Intentional Elements in the grl graph
        CreateAllLinkRefCommand comm2 = new CreateAllLinkRefCommand(objRef);
        comm2.execute();        
        
        // To collect the IntentionalElements refs of all the elements that already exist in the graph and also are in level one of the chosen element        
        List<IntentionalElementRef> tempRefList;
        levelTwoFinalIntentionalElementRefList = new ArrayList<IntentionalElementRef>();
        
        if (levelTwoSrc == true)
        {
            srcGRLLinkableElementList = new ArrayList<GRLLinkableElement>(levelTwoSrcGRLLinkableElementList);
            
            for (int i = 0; i < levelTwoSrcGRLLinkableElementList.size(); i++)
            {   
                tempRefList = new ArrayList<IntentionalElementRef>(( (IntentionalElement) levelTwoSrcGRLLinkableElementList.get(i)).getRefs());
                
                for (IntentionalElementRef IER : tempRefList)
                    if (( (GRLGraph) IER.getDiagram()).equals(grlGraph))
                        levelTwoFinalIntentionalElementRefList.add(IER);
            }
        }
        if (levelTwoDest == true)
        {
            destGRLLinkableElementList = new ArrayList<GRLLinkableElement>(levelTwoDestGRLLinkableElementList);
            
            for (int i = 0; i < levelTwoDestGRLLinkableElementList.size(); i++)
            {   
                tempRefList = new ArrayList<IntentionalElementRef>(( (IntentionalElement) levelTwoDestGRLLinkableElementList.get(i)).getRefs());
                
                for (IntentionalElementRef IER : tempRefList)
                    if (( (GRLGraph) IER.getDiagram()).equals(grlGraph))
                        levelTwoFinalIntentionalElementRefList.add(IER);
            }
        }
        
        // Adding second and third level of connected Intentional Elements by calling command ShowLinkedElementLevelTwoCommand on all the connected
        // elements, which were found as directly-connected elements 
        int index = 0;
        
        if (levelTwoSrc == false)
        {
            for (index = 0; index < srcGRLLinkableElementList.size(); index++)
            {   // srcGRLLinkableElementList.get(i) or finalIntentionalElementRefList.get(index).getDef()
                Command comm = new ShowLinkedElementLevelTwoCommand(this.urnspec, srcGRLLinkableElementList.get(index), 
                    finalIntentionalElementRefList.get(index));
                comm.execute();
            }
        }
        else
        {
            for (index = 0; index < srcGRLLinkableElementList.size(); index++)
            {   // srcGRLLinkableElementList.get(i) or finalIntentionalElementRefList.get(index).getDef()
                Command comm = new ShowLinkedElementLevelTwoCommand(this.urnspec, srcGRLLinkableElementList.get(index), 
                    levelTwoFinalIntentionalElementRefList.get(index));
                comm.execute();
            }
        }
        
        if (levelTwoDest == false)
        {
            if (levelTwoSrc == true)
                index = 0;
            
            for (int i = 0; i < destGRLLinkableElementList.size(); i++)
            {   // destGRLLinkableElementList.get(i) or finalIntentionalElementRefList.get(index).getDef()   
                Command comm = new ShowLinkedElementLevelTwoCommand(this.urnspec, destGRLLinkableElementList.get(i), 
                    finalIntentionalElementRefList.get(index));
                comm.execute();
                index++;
            }
        }
        else
        {
            if (levelTwoSrc == false)
                index = 0;
            
            for (int i = 0; i < destGRLLinkableElementList.size(); i++)
            {   // destGRLLinkableElementList.get(i) or finalIntentionalElementRefList.get(index).getDef()   
                Command comm = new ShowLinkedElementLevelTwoCommand(this.urnspec, destGRLLinkableElementList.get(i), 
                    levelTwoFinalIntentionalElementRefList.get(index));
                comm.execute();
                index++;
            }
        }        
        */
        testPostConditions();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPostConditions()
     */
    public void testPostConditions() 
    {
        assert grlelem != null : "post no elemement to modify!"; //$NON-NLS-1$
        assert urnspec != null : "post no URN specification to modify!"; //$NON-NLS-1$
        assert grlGraph != null : "post no grl Grpah to modify"; //$NON-NLS-1$
        assert objRef != null : "post no objRef to modify"; //$NON-NLS-1$
        assert diagramOfElement != null : "post no diagram to modify"; //$NON-NLS-1$
        assert chosenIntentionalElement != null : "post no Intentional Element to modify"; //$NON-NLS-1$
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPreConditions()
     */
    public void testPreConditions() 
    {
        assert grlelem != null : "pre no elemement to modify!"; //$NON-NLS-1$
        assert urnspec != null : "pre no URN specification to modify!"; //$NON-NLS-1$
        assert grlGraph != null : "pre no grl Grpah to modify"; //$NON-NLS-1$
        assert objRef != null : "pre no objRef to modify"; //$NON-NLS-1$
        assert diagramOfElement != null : "pre no diagram to modify"; //$NON-NLS-1$
        assert chosenIntentionalElement != null : "pre no Intentional Element to modify"; //$NON-NLS-1$
    }
    
    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() 
    {
        testPostConditions();
        
        List<IntentionalElementRef> listOfIntentionalElementRefs = new ArrayList<IntentionalElementRef>(objRef.getDiagram().getNodes());
        listOfIntentionalElementRefs.removeAll(currentIntentionalElementRefList);
        
        for (int i = 0; i < listOfIntentionalElementRefs.size(); i++)
        {
            DeleteAllLinkRefCommand comm1 = new DeleteAllLinkRefCommand(listOfIntentionalElementRefs.get(i));
            comm1.execute();
        
            DisconnectGRLNodeCommand comm2 = new DisconnectGRLNodeCommand(listOfIntentionalElementRefs.get(i));
            comm2.execute();
        
            RemoveURNmodelElementCommand comm3 = new RemoveURNmodelElementCommand(listOfIntentionalElementRefs.get(i));
            comm3.execute();
        }
        
        //currentIntentionalElementRefList = new ArrayList<IntentionalElementRef>(grlGraph.getNodes());
        //finalIntentionalElementRefList.clear();
        //addedIntentionalElementRefList.clear();
        
        testPreConditions();
    }
}