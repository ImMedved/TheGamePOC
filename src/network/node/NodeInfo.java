package network.node;

import java.security.PublicKey;

public final class NodeInfo {

    public final long nodeId;
    public final String ip;
    public final int port;
    public final PublicKey publicKey;

    public NodeInfo(long nodeId, String ip, int port, PublicKey publicKey) {
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
        this.publicKey = publicKey;
    }
}