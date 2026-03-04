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

    public static NetworkNode start(NetworkConfig config) {

        PacketSerializer serializer = new PacketSerializer();

        CryptoModule crypto = new CryptoModule();

        KeyPairData keys = crypto.generateKeyPair();
        NodeId localId = new NodeId(1);

        NetworkNode node =
                new NetworkNode(
                        localId,
                        serializer,
                        crypto,
                        keys.privateKey()
                );

        if (config.host) {

            ConnectionListener listener =
                    new ConnectionListener(config.port);

            listener.start(socket -> {

                P2PConnection conn =
                        new P2PConnection(socket);

                node.addPeer(
                        new NodeId(2),
                        conn,
                        keys.publicKey()
                );

            });

        } else {

            try {

                Socket socket =
                        new Socket(config.peerIp, config.peerPort);

                P2PConnection conn =
                        new P2PConnection(socket);

                node.addPeer(
                        new NodeId(2),
                        conn,
                        keys.publicKey()
                );

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return node;
    }
}