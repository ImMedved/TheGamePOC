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
        System.out.println("[NET] Starting node on port " + config.port + " host=" + config.host);
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

            System.out.println("[NET] Incoming connection from " + socket.getRemoteSocketAddress());

            P2PConnection conn = new P2PConnection(socket);

        });

        for (NodeInfo info : topology.nodes()) {

            if (info.nodeId <= nodeId)
                continue;

            try {

                Socket socket = new Socket(info.ip, info.port);

                System.out.println("[NET] Connected to peer NodeId[" + info.nodeId + "]");

                P2PConnection conn = new P2PConnection(socket);

                node.addPeer(
                        new NodeId(info.nodeId),
                        conn,
                        info.publicKey
                );

            } catch (Exception e) {

                System.out.println("[NET] Failed connect to NodeId[" + info.nodeId + "]");

            }
        }

        return node;
    }
}