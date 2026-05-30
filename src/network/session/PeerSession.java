package network.session;

import network.crypto.CryptoModule;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.transport.P2PConnection;

import java.security.PublicKey;
import java.util.Arrays;
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
                util.Log.warn("[NET] Rejected packet with invalid signature from " + peerId);
                return;
            }

            if (!checkSequence(packet)) {
                util.Log.warn("[NET] Rejected stale packet from " + peerId +
                        " seq=" + packet.sequenceNumber());
                return;
            }

            packetHandler.accept(packet);
            util.Log.debug("[NET] Packet received type=" + packet.type() +
                    " tick=" + packet.tickNumber() + " seq=" + packet.sequenceNumber());
        });
    }

    private boolean verifyPacket(NetworkPacket packet) {

        NetworkPacket unsigned =
                new NetworkPacket(
                        packet.sender(),
                        packet.sequenceNumber(),
                        packet.tickNumber(),
                        packet.type(),
                        packet.payload(),
                        null
                );

        byte[] serialized =
                serializer.serialize(unsigned);
        boolean ok = crypto.verify(
                serialized,
                packet.signature(),
                peerPublicKey);

        byte[] serializedCounter = serializer.serialize(unsigned);
        util.Log.debug("[NET] Verify sender=" + packet.sender() +
                " seq=" + packet.sequenceNumber() +
                " tick=" + packet.tickNumber() +
                " bytes=" + serializedCounter.length +
                " result=" + ok);
        return ok;
    }

    private boolean checkSequence(NetworkPacket packet) {

        int seq = packet.sequenceNumber();

        if (seq <= lastReceivedSequence)
            return false;

        util.Log.debug("[NET] Sequence check last=" + lastReceivedSequence + " new=" + seq);
        lastReceivedSequence = seq;
        return true;
    }

    public void sendPacket(NetworkPacket packet) {
        util.Log.debug("[NET] SEND packet type=" + packet.type() +
                " tick=" + packet.tickNumber() + " seq=" + packet.sequenceNumber());

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
