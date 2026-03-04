package network.session;

import network.crypto.CryptoModule;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.transport.P2PConnection;

import java.security.PublicKey;
import java.util.function.Consumer;

public final class PeerSession {

    private final NodeId peerId;

    private final P2PConnection connection;

    private final PacketSerializer serializer;

    private final CryptoModule crypto;

    private final PublicKey peerPublicKey;

    private final Consumer<NetworkPacket> packetHandler;

    private int lastReceivedSequence = -1;

    public PeerSession(
            NodeId peerId,
            P2PConnection connection,
            PacketSerializer serializer,
            CryptoModule crypto,
            PublicKey peerPublicKey,
            Consumer<NetworkPacket> packetHandler
    ) {

        this.peerId = peerId;
        this.connection = connection;
        this.serializer = serializer;
        this.crypto = crypto;
        this.peerPublicKey = peerPublicKey;
        this.packetHandler = packetHandler;

        startReceiving();
    }

    private void startReceiving() {

        connection.startReceiving(data -> {

            NetworkPacket packet =
                    serializer.deserialize(data);

            if (!verifyPacket(packet)) {
                return;
            }

            if (!checkSequence(packet)) {
                return;
            }

            packetHandler.accept(packet);

        });
    }

    private boolean verifyPacket(NetworkPacket packet) {

        if (packet.signature() == null)
            return false;

        byte[] payload = packet.payload();

        return crypto.verify(
                payload,
                packet.signature(),
                peerPublicKey
        );
    }

    private boolean checkSequence(NetworkPacket packet) {

        int seq = packet.sequenceNumber();

        if (seq <= lastReceivedSequence)
            return false;

        lastReceivedSequence = seq;

        return true;
    }

    public void sendPacket(NetworkPacket packet) {

        byte[] data =
                serializer.serialize(packet);

        connection.send(data);
    }

    public NodeId peerId() {
        return peerId;
    }

    public void close() {
        connection.close();
    }
}