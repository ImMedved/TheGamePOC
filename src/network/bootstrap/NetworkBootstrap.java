package network.bootstrap;

import network.config.NetworkConfig;
import network.config.NetworkTopology;
import network.crypto.CryptoModule;
import network.crypto.KeyPairData;
import network.model.NodeId;
import network.node.NetworkNode;
import network.node.NodeInfo;
import network.protocol.PacketSerializer;
import network.transport.ConnectionListener;
import network.transport.P2PConnection;

import java.net.Socket;
import java.security.KeyPair;

public final class NetworkBootstrap {

    public static NetworkNode start(NetworkConfig config, long nodeId, NetworkTopology topology) {
        util.Log.info("[NET] Starting node on port " + config.port + " host=" + config.host);
        PacketSerializer serializer = new PacketSerializer();
        CryptoModule crypto = new CryptoModule();

        NodeId localId = new NodeId(nodeId);

        NetworkNode node =
                new NetworkNode(
                        nodeId,
                        localId,
                        serializer,
                        crypto,
                        config.privateKey
                );

        ConnectionListener listener = new ConnectionListener(config.port);

        listener.start(socket -> {

            util.Log.info("[NET] Incoming connection from " + socket.getRemoteSocketAddress());

            P2PConnection conn = new P2PConnection(socket);
            long remoteId = conn.readPeerNodeId();
            conn.sendLocalNodeId(nodeId);

            NodeInfo info = topology.get(remoteId);

            node.addPeer(
                    new NodeId(remoteId),
                    conn,
                    info.publicKey
            );

        });

        for (NodeInfo info : topology.nodes()) {

            if (info.nodeId <= nodeId)
                continue;

            while (true) {
                try {

                    Socket socket = new Socket(info.ip, info.port);

                    util.Log.info("[NET] Connected to peer NodeId[" + info.nodeId + "]");

                    P2PConnection conn = new P2PConnection(socket);
                    conn.sendLocalNodeId(nodeId);
                    long remoteId = conn.readPeerNodeId();

                    if (remoteId != info.nodeId) {
                        throw new IllegalStateException(
                                "Connected to unexpected node. expected=" + info.nodeId +
                                        " actual=" + remoteId
                        );
                    }

                    node.addPeer(
                            new NodeId(info.nodeId),
                            conn,
                            info.publicKey
                    );

                    break;

                } catch (Exception e) {
                    util.Log.debug("[NET] Waiting for NodeId[" + info.nodeId +
                            "]: " + e.getMessage());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Network bootstrap interrupted", interrupted);
                    }

                }
            }
        }

        return node;
    }

}
