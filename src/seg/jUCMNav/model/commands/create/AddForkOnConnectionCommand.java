package seg.jUCMNav.model.commands.create;

import org.eclipse.gef.commands.Command;

import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.JUCMNavCommand;
import seg.jUCMNav.model.util.ParentFinder;
import ucm.map.EmptyPoint;
import ucm.map.EndPoint;
import ucm.map.Map;
import ucm.map.NodeConnection;
import ucm.map.PathGraph;
import ucm.map.PathNode;
import urn.URNspec;

/**
 * @author jpdaigle
 *  
 */
public class AddForkOnConnectionCommand extends Command implements JUCMNavCommand {
    // Where to create the Fork
    PathGraph _pg;

    EmptyPoint _originEp;

    // New items
    PathNode _newFork;

    EmptyPoint _newEmptyPoint;

    EndPoint _newEndPoint;

    NodeConnection _newLink1, _newLink2, _ncPred, _ncTarg;

    NodeConnection _originNc;

    PathNode _prevNode, _nextNode;

    int _posX, _posY;

    /**
     *  
     */
    public AddForkOnConnectionCommand() {
        super();
        this.setLabel("Add Fork");
    }

    public AddForkOnConnectionCommand(PathNode newFork, PathGraph pg, NodeConnection nc, int x, int y) {
        _pg = pg;
        _originNc = nc;
        _posX = x;
        _posY = y;
        _newFork = newFork;
        this.setLabel("Add Fork");
    }

    public boolean canExecute() {
        return super.canExecute();
    }

    public void execute() {
        // Well, this isn't very clean :(
        // HACK HACK HACK HACK HACK HACK HACK HACK

        // Split existing connection
        _prevNode = _originNc.getSource();
        _nextNode = _originNc.getTarget();
        _ncTarg = (NodeConnection) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer()
                .eContainer(), NodeConnection.class);
        _ncPred = _originNc;

        // Fork -- EmptyPoint -- EndPoint
//        _newFork = (OrFork) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer().eContainer(),
//                OrFork.class);
        _newFork.setX(_posX);
        _newFork.setY(_posY);

        _newEmptyPoint = (EmptyPoint) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer()
                .eContainer(), EmptyPoint.class);
        _newEmptyPoint.setX(_posX + 25);
        _newEmptyPoint.setY(_posY - 25);

        _newLink1 = (NodeConnection) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer()
                .eContainer(), NodeConnection.class);

        _newEndPoint = (EndPoint) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer()
                .eContainer(), EndPoint.class);
        _newEndPoint.setX(_posX + 75);
        _newEndPoint.setY(_posY - 30);

        _newLink2 = (NodeConnection) ModelCreationFactory.getNewObject((URNspec) _pg.eContainer().eContainer()
                .eContainer(), NodeConnection.class);

        // TODO Add an empty point *ON* the connection going towards the
        // EndPoint

        redo();
    }

    public void redo() {
        // _ncPred is now the connection the user clicked on
        _ncTarg.setTarget(_nextNode);
        _ncTarg.setSource(_newFork);
        _ncPred.setTarget(_newFork);

        _newLink1.setSource(_newFork);
        _newLink1.setTarget(_newEmptyPoint);

        _newLink2.setSource(_newEmptyPoint);
        _newLink2.setTarget(_newEndPoint);

        _pg.getNodeConnections().add(_newLink1);
        _pg.getNodeConnections().add(_newLink2);
        _pg.getNodeConnections().add(_ncTarg);

        _pg.getPathNodes().add(_newFork);
        _pg.getPathNodes().add(_newEmptyPoint);
        _pg.getPathNodes().add(_newEndPoint);

        _newFork.setCompRef(ParentFinder.findParent((Map) _pg.eContainer(), _newFork.getX(), _newFork.getY()));
        _newEmptyPoint.setCompRef(ParentFinder.findParent((Map) _pg.eContainer(), _newEmptyPoint.getX(), _newEmptyPoint.getY()));
        _newEndPoint.setCompRef(ParentFinder.findParent((Map) _pg.eContainer(), _newEndPoint.getX(), _newEndPoint.getY()));
    }
    
    public void undo() {
        _ncPred.setTarget(_nextNode);

        _ncTarg.setTarget(null);
        _ncTarg.setSource(null);

        _newLink1.setSource(null);
        _newLink1.setTarget(null);

        _newLink2.setSource(null);
        _newLink2.setTarget(null);

        _pg.getNodeConnections().remove(_newLink1);
        _pg.getNodeConnections().remove(_newLink2);
        _pg.getNodeConnections().remove(_ncTarg);

        _pg.getPathNodes().remove(_newFork);
        _pg.getPathNodes().remove(_newEmptyPoint);
        _pg.getPathNodes().remove(_newEndPoint);

        _newFork.setCompRef(null);
        _newEmptyPoint.setCompRef(null);
        _newEndPoint.setCompRef(null);
    }
    
    /**
     * @param label
     */
    public AddForkOnConnectionCommand(String label) {
        super(label);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPreConditions()
     */
    public void testPreConditions() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.model.commands.JUCMNavCommand#testPostConditions()
     */
    public void testPostConditions() {
        // TODO Auto-generated method stub

    }

}