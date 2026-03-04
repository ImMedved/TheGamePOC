package network.model;

import java.net.InetSocketAddress;

public final class PeerInfo {

    private final NodeId nodeId;

    private final byte[] publicKey;

    private final InetSocketAddress address;

    public PeerInfo(NodeId nodeId,
                    byte[] publicKey,
                    InetSocketAddress address) {

        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.address = address;
    }

    public NodeId nodeId() {
        return nodeId;
    }

    public byte[] publicKey() {
        return publicKey;
    }

    public InetSocketAddress address() {
        return address;
    }
}