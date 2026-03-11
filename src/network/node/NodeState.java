package network.node;

public final class NodeState {

    public final long nodeId;

    public NodeRole role;

    public NodeState(long nodeId) {
        this.nodeId = nodeId;
    }
}