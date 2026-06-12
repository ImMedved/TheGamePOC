package network.session;

import network.crypto.CryptoModule;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.transport.P2PConnection;

import java.security.PublicKey;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PeerSession {

    private final NodeId peerId;

    private final P2PConnection connection;

    private final PacketSerializer serializer;

    private final CryptoModule crypto;

    private final PublicKey peerPublicKey;

    private final Consumer<NetworkPacket> packetHandler;
    private final BiConsumer<NodeId, NetworkPacket> invalidPacketHandler;

    private int lastReceivedSequence = -1;

    public PeerSession(
            NodeId peerId,
            P2PConnection connection,
            PacketSerializer serializer,
            CryptoModule crypto,
            PublicKey peerPublicKey,
            Consumer<NetworkPacket> packetHandler,
            BiConsumer<NodeId, NetworkPacket> invalidPacketHandler
    ) {

        this.peerId = peerId;
        this.connection = connection;
        this.serializer = serializer;
        this.crypto = crypto;
        this.peerPublicKey = peerPublicKey;
        this.packetHandler = packetHandler;
        this.invalidPacketHandler = invalidPacketHandler;

        startReceiving();

    }

    private void startReceiving() {
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] PeerSession start peer=" + peerId);
        }

        connection.startReceiving(data -> {

            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[NET] Packet buffer received peer=" + peerId + " bytes=" + data.length);
            }

            NetworkPacket packet =
                    serializer.deserialize(data);

            if (!verifyPacket(packet)) {
                util.Log.warn("[NET] Rejected packet with invalid signature from " + peerId);
                invalidPacketHandler.accept(peerId, packet);
                return;
            }

            if (!checkSequence(packet)) {
                util.Log.warn("[NET] Rejected stale packet from " + peerId +
                        " seq=" + packet.sequenceNumber());
                return;
            }

            packetHandler.accept(packet);
            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[NET] Packet received type=" + packet.type() +
                        " tick=" + packet.tickNumber() + " seq=" + packet.sequenceNumber() +
                        " sender=" + packet.sender() +
                        " payload=" + packet.payload().length +
                        " signature=" + (packet.signature() == null ? 0 : packet.signature().length));
            }
        });
    }

    private boolean verifyPacket(NetworkPacket packet) {

        if (!peerId.equals(packet.sender())) {
            util.Log.warn("[NET] Rejected packet with spoofed sender=" + packet.sender()
                    + " on session=" + peerId);
            return false;
        }

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

        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] Verify sender=" + packet.sender() +
                    " seq=" + packet.sequenceNumber() +
                    " tick=" + packet.tickNumber() +
                    " bytes=" + serialized.length +
                    " payload=" + packet.payload().length +
                    " signature=" + (packet.signature() == null ? 0 : packet.signature().length) +
                    " result=" + ok);
        }
        return ok;
    }

    private boolean checkSequence(NetworkPacket packet) {

        int seq = packet.sequenceNumber();

        if (seq <= lastReceivedSequence)
            return false;

        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] Sequence check peer=" + peerId +
                    " last=" + lastReceivedSequence + " new=" + seq);
        }
        lastReceivedSequence = seq;
        return true;
    }

    public void sendPacket(NetworkPacket packet) {
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] SEND packet type=" + packet.type() +
                    " tick=" + packet.tickNumber() + " seq=" + packet.sequenceNumber() +
                    " peer=" + peerId);
        }

        byte[] data =
                serializer.serialize(packet);
        connection.send(data);
    }

    public void close() {
        connection.close();
    }
}
