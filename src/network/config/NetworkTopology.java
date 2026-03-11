package network.config;

import network.node.NodeInfo;

import java.util.List;

public final class NetworkTopology {

    private final List<NodeInfo> nodes;

    public NetworkTopology(List<NodeInfo> nodes) {
        this.nodes = List.copyOf(nodes);
    }

    public List<NodeInfo> nodes() {
        return nodes;
    }

    public NodeInfo get(long nodeId) {
        for (NodeInfo n : nodes) {
            if (n.nodeId == nodeId) return n;
        }
        return null;
    }
}