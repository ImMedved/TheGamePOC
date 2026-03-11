package network.bootstrap;

import network.config.NetworkConfig;
import network.crypto.CryptoModule;
import network.crypto.KeyPairData;
import network.model.NodeId;
import network.node.NetworkNode;
import network.protocol.PacketSerializer;
import network.transport.ConnectionListener;
import network.transport.P2PConnection;

import java.net.Socket;
import java.security.KeyPair;

public final class NetworkBootstrap {

    public static NetworkNode start(NetworkConfig config, long nodeId) {
        System.out.println("[NET] Starting node on port " + config.port + " host=" + config.host);
        PacketSerializer serializer = new PacketSerializer();
        CryptoModule crypto = new CryptoModule();

        NodeId localId = new NodeId(config.host ? 1 : 2);
        NodeId peerId = new NodeId(config.host ? 2 : 1);

        NetworkNode node =
                new NetworkNode(
                        nodeId,
                        localId,
                        serializer,
                        crypto,
                        config.privateKey
                );

        if (config.host) {
            ConnectionListener listener = new ConnectionListener(config.port);
            listener.start(socket -> {
                System.out.println("[NET] Incoming connection from " + socket.getRemoteSocketAddress());
                P2PConnection conn = new P2PConnection(socket);

                node.addPeer(
                        peerId,
                        conn,
                        config.peerPublicKey
                );
            });
        } else {
            try {
                Socket socket = new Socket(config.peerIp, config.peerPort);
                System.out.println("[NET] Connected to peer " + socket.getRemoteSocketAddress());
                P2PConnection conn = new P2PConnection(socket);
                node.addPeer(
                        peerId,
                        conn,
                        config.peerPublicKey
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return node;
    }
}