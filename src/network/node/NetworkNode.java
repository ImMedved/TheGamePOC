package network.node;

import network.consensus.ValidatorSelector;
import network.crypto.CryptoModule;
import network.lockstep.LockstepSynchronizer;
import network.model.GameStartPayload;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.session.PeerSession;
import network.transport.P2PConnection;
import network.validation.StateHashValidator;
import network.validation.ValidationPayload;
import network.vdf.VDFModule;
import network.vdf.VDFSeed;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NetworkNode {

    private final NodeId localNodeId;

    private final PacketSerializer serializer;
    private final CryptoModule crypto;

    public final long nodeId;
    private final Map<NodeId, PeerSession> sessions = new HashMap<>();
    private final LockstepSynchronizer lockstep;
    private final StateHashValidator validator;
    private final PrivateKey privateKey;

    private int sequenceCounter = 0;
    private final ExecutorService consensusExecutor =
            Executors.newSingleThreadExecutor();

    public NetworkNode(
            long nodeId,
            NodeId localNodeId,
            PacketSerializer serializer,
            CryptoModule crypto,
            PrivateKey privateKey
    ) {
        this.nodeId = nodeId;
        this.localNodeId = localNodeId;
        this.serializer = serializer;
        this.crypto = crypto;
        this.privateKey = privateKey;

        this.lockstep = new LockstepSynchronizer(localNodeId);
        this.validator = new StateHashValidator(60, this::onDesync);
    }

    public void startGame(UUID gameId, long playerA, long playerB) {
        System.out.println("[NETWORK] GAME_START received");
        GameStartPayload payload =
                new GameStartPayload(gameId, playerA, playerB);

        byte[] payloadBytes = payload.toBytes();

        int seq = sequenceCounter++;

        int tick = 0;

        NetworkPacket unsigned =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.GAME_START,
                        payloadBytes,
                        null
                );

        byte[] serialized = serializer.serialize(unsigned);

        byte[] signature = crypto.sign(serialized, privateKey);

        NetworkPacket signed =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.GAME_START,
                        payloadBytes,
                        signature
                );

        broadcast(signed);
        System.out.println("[NETWORK] GAME_START sent seq=" + seq);

    }

    public void addPeer(
            NodeId peerId,
            P2PConnection connection,
            PublicKey peerKey
    ) {
        System.out.println("[NET] Peer added: " + peerId);
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
        System.out.println("[NET] Handling packet type=" + packet.type() + " tick=" + packet.tickNumber());
        switch (packet.type()) {

            case INPUT -> {

                lockstep.receiveRemoteInput(
                        packet.sender(),
                        packet.tickNumber(),
                        packet.payload()
                );

                if (isValidator) {
                    validateMove(packet);
                }
            }

            case STATE_HASH -> {

                int tick = packet.tickNumber();

                validator.receiveRemoteHash(
                        peer,
                        tick,
                        packet.payload()
                );
            }
            case GAME_START -> handleGameStart(packet);

            case VOID -> handleVoid(packet);
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

        byte[] payloadCopy = payload.clone();

        NetworkPacket unsignedPacket =
                new NetworkPacket(
                        localNodeId,
                        sequenceCounter++,
                        tick,
                        type,
                        payloadCopy,
                        null
                );

        byte[] serialized = serializer.serialize(unsignedPacket);
        byte[] signature = crypto.sign(serialized, privateKey);

        return new NetworkPacket(
                localNodeId,
                unsignedPacket.sequenceNumber(),
                tick,
                type,
                payloadCopy,
                signature
        );
    }

    private void broadcast(NetworkPacket packet) {

        for (PeerSession session : sessions.values()) {

            session.sendPacket(packet);
        }
    }

    private void onDesync(Integer tick) {

        System.out.println("[NET] DESYNC detected at tick: " + tick);
    }
    public Map<NodeId, byte[]> waitForInputs(int tick) {
        return lockstep.waitForInputs(tick);
    }

    private volatile UUID currentGameId;
    private final VDFModule vdf = new VDFModule();
    private final ValidatorSelector selector = new ValidatorSelector();

    private NodeId validatorNodeId;
    private boolean isValidator;

    private void handleGameStart(NetworkPacket packet) {

        GameStartPayload payload = GameStartPayload.fromBytes(packet.payload());

        if (currentGameId != null)
            return;
        currentGameId = payload.gameId;
        System.out.println("[GAME][NET] start " + payload.gameId);

        consensusExecutor.submit(() -> runVdfAndSelectValidator(payload));
    }

    private static String bytesToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private void runVdfAndSelectValidator(GameStartPayload payload) {

        List<Long> nodeIds = List.of(1L, 2L, 3L);

        byte[] seed = VDFSeed.create(
                payload.gameId,
                nodeIds
        );

        byte[] entropy = vdf.compute(seed);

        System.out.println("[VDF][NET] entropy=" + bytesToHex(entropy));

        long validator = selector.selectValidator(
                entropy,
                nodeIds
        );

        validatorNodeId = new NodeId(validator);

        isValidator = validatorNodeId.equals(localNodeId);

        System.out.println("[CONSENSUS] validator=" + validatorNodeId);

        if (isValidator) {
            System.out.println("[ROLE] this node is VALIDATOR");
        } else {
            System.out.println("[ROLE] this node is PLAYER");
        }
    }

    private int lastTickA = -1;
    private int lastTickB = -1;

    private void validateMove(NetworkPacket packet) {

        int tick = packet.tickNumber();

        long player = packet.sender().value();

        boolean valid = true;

        if (player == 1) {

            if (tick <= lastTickA) {
                valid = false;
            }

            lastTickA = tick;

        } else if (player == 2) {

            if (tick <= lastTickB) {
                valid = false;
            }

            lastTickB = tick;
        }

        if (!valid) {

            System.out.println("[VALIDATOR] invalid move tick=" + tick);

            sendVoid(tick);

        } else {

            sendValidation(tick);
        }
    }

    private void sendValidation(int tick) {

        ValidationPayload payload = new ValidationPayload(currentGameId, tick, true);

        byte[] payloadBytes = payload.toBytes();

        int seq = sequenceCounter++;

        NetworkPacket unsigned =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.VALIDATION,
                        payloadBytes,
                        null
                );

        byte[] serialized = serializer.serialize(unsigned);

        byte[] signature = crypto.sign(serialized, privateKey);

        NetworkPacket signed =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.VALIDATION,
                        payloadBytes,
                        signature
                );

        broadcast(signed);
    }

    private void sendVoid(int tick) {

        ValidationPayload payload =
                new ValidationPayload(currentGameId, tick, false);

        byte[] payloadBytes = payload.toBytes();

        int seq = sequenceCounter++;

        NetworkPacket unsigned =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.VOID,
                        payloadBytes,
                        null
                );

        byte[] serialized = serializer.serialize(unsigned);

        byte[] signature = crypto.sign(serialized, privateKey);

        NetworkPacket signed =
                new NetworkPacket(
                        localNodeId,
                        seq,
                        tick,
                        NetworkPacket.PacketType.VOID,
                        payloadBytes,
                        signature
                );

        broadcast(signed);
    }

    private void handleVoid(NetworkPacket packet) {

        ValidationPayload payload = ValidationPayload.fromBytes(packet.payload());

        System.out.println("[GAME] VOID detected at tick " + payload.tick);

        // остановить игру
    }
}