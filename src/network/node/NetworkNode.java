package network.node;

import network.crypto.CryptoModule;
import network.lockstep.LockstepSynchronizer;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.session.PeerSession;
import network.transport.P2PConnection;
import network.validation.StateHashValidator;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class NetworkNode {

    private final NodeId localNodeId;

    private final PacketSerializer serializer;
    private final CryptoModule crypto;

    private final Map<NodeId, PeerSession> sessions = new HashMap<>();

    private final LockstepSynchronizer lockstep;

    private final StateHashValidator validator;

    private final PrivateKey privateKey;

    private int sequenceCounter = 0;


    public NetworkNode(
            NodeId localNodeId,
            PacketSerializer serializer,
            CryptoModule crypto,
            PrivateKey privateKey
    ) {

        this.localNodeId = localNodeId;
        this.serializer = serializer;
        this.crypto = crypto;
        this.privateKey = privateKey;

        this.lockstep = new LockstepSynchronizer(localNodeId);

        this.validator =
                new StateHashValidator(
                        60,
                        this::onDesync
                );
    }

    public void addPeer(
            NodeId peerId,
            P2PConnection connection,
            PublicKey peerKey
    ) {

        PeerSession session =
                new PeerSession(
                        peerId,
                        connection,
                        serializer,
                        crypto,
                        peerKey,
                        packet -> handlePacket(peerId, packet)
                );

        sessions.put(peerId, session);
    }

    private void handlePacket(
            NodeId peer,
            NetworkPacket packet
    ) {

        switch (packet.type()) {

            case INPUT -> {

                int tick = packet.tickNumber();

                lockstep.receiveRemoteInput(
                        peer,
                        tick,
                        packet.payload()
                );
            }

            case STATE_HASH -> {

                int tick = packet.tickNumber();

                validator.receiveRemoteHash(
                        peer,
                        tick,
                        packet.payload()
                );
            }
        }
    }

    public void submitLocalInput(int tick, byte[] input) {

        lockstep.submitLocalInput(tick, input);

        NetworkPacket packet =
                createPacket(
                        NetworkPacket.PacketType.INPUT,
                        tick,
                        input
                );

        broadcast(packet);
    }

    public Map<NodeId, byte[]> tryGetInputs(int tick) {
        return lockstep.tryGetInputs(tick);
    }

    public void submitStateHash(int tick, byte[] hash) {

        validator.storeLocalHash(tick, hash);

        NetworkPacket packet =
                createPacket(
                        NetworkPacket.PacketType.STATE_HASH,
                        tick,
                        hash
                );

        broadcast(packet);
    }

    private NetworkPacket createPacket(
            NetworkPacket.PacketType type,
            int tick,
            byte[] payload
    ) {

        NetworkPacket unsignedPacket =
                new NetworkPacket(
                        localNodeId,
                        sequenceCounter++,
                        tick,
                        type,
                        payload,
                        null
                );

        byte[] serialized =
                serializer.serialize(unsignedPacket);

        byte[] signature =
                crypto.sign(serialized, privateKey);

        return new NetworkPacket(
                localNodeId,
                unsignedPacket.sequenceNumber(),
                tick,
                type,
                payload,
                signature
        );
    }

    private void broadcast(NetworkPacket packet) {

        for (PeerSession session : sessions.values()) {

            session.sendPacket(packet);
        }
    }

    private void onDesync(Integer tick) {

        System.out.println("DESYNC detected at tick: " + tick);
    }
}