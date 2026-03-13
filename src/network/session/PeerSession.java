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
                return;
            }

            if (!checkSequence(packet)) {
                return;
            }

            packetHandler.accept(packet);
            System.out.println("[NET] Packet received type=" + packet.type() + " tick=" + packet.tickNumber() + " seq=" + packet.sequenceNumber());
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
        //System.out.println("[NET] VERIFY sender=" + packet.sender());
        //System.out.println("[NET] VERIFY seq=" + packet.sequenceNumber());
        //System.out.println("[NET] VERIFY tick=" + packet.tickNumber());
        boolean ok = crypto.verify(
                serialized,
                packet.signature(),
                peerPublicKey);

        //System.out.println("[NET] Verify result=" + ok);
        byte[] serializedCounter = serializer.serialize(unsigned);
        //System.out.println("[NET] VERIFY bytes=" + serializedCounter.length);
        return ok;
    }

    private boolean checkSequence(NetworkPacket packet) {

        int seq = packet.sequenceNumber();

        if (seq <= lastReceivedSequence)
            return false;

        System.out.println("[NET] Sequence check: last=" + lastReceivedSequence + " new=" + seq);
        lastReceivedSequence = seq;
        return true;
    }

    public void sendPacket(NetworkPacket packet) {
        System.out.println("[NET] SEND packet type=" + packet.type()
                + " tick=" + packet.tickNumber()
                + " seq=" + packet.sequenceNumber());

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