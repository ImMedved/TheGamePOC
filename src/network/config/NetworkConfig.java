package network.config;

import java.security.PrivateKey;
import java.security.PublicKey;

public final class NetworkConfig {
    public final long nodeId;
    public final int port;
    public final String peerIp;
    public final int peerPort;
    public final boolean host;

    public final PrivateKey privateKey;
    public final PublicKey peerPublicKey;

    public NetworkConfig(
            long nodeId,
            boolean host,
            String peerIp,
            int port,
            int peerPort,
            PrivateKey privateKey,
            PublicKey peerPublicKey
    ) {
        this.nodeId = nodeId;
        this.host = host;
        this.peerIp = peerIp;
        this.port = port;
        this.peerPort = peerPort;
        this.privateKey = privateKey;
        this.peerPublicKey = peerPublicKey;
    }
}