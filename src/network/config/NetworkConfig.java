package network.config;

public final class NetworkConfig {

    public final int port;

    public final String peerIp;

    public final int peerPort;

    public final boolean host;

    public NetworkConfig(
            boolean host,
            String peerIp,
            int port,
            int peerPort
    ) {
        this.host = host;
        this.peerIp = peerIp;
        this.port = port;
        this.peerPort = peerPort;
    }
}