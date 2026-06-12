package network.node;

import core.registries.ProjectileRegistry;
import core.states.WorldState;
import core.systems.GameSystem;
import network.crypto.CryptoModule;
import network.lockstep.LockstepSynchronizer;
import network.model.CharacterSelectPayload;
import network.model.GameStartPayload;
import network.model.NetworkPacket;
import network.model.NodeId;
import network.protocol.PacketSerializer;
import network.session.PeerSession;
import network.transport.P2PConnection;
import network.validation.RuleValidationEngine;
import network.validation.StateFramePayload;
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
    private RuleValidationEngine ruleValidator;
    private final PrivateKey privateKey;
    private final Map<Long, Integer> selectedCharacters = new HashMap<>();

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
    }

    public void startGame(UUID gameId, long playerA, long playerB) {
        util.Log.info("[GAME][NET] Host starting game " + gameId);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[GAME][NET] startGame players=" + playerA + "," + playerB +
                    " peers=" + sessions.size());
        }
        GameStartPayload payload =
                new GameStartPayload(gameId, playerA, playerB);

        currentGameId = gameId;
        consensusExecutor.submit(() -> runVdfAndSelectValidator(payload));

        byte[] payloadBytes = payload.toBytes();
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[GAME][NET] GAME_START payloadBytes=" + payloadBytes.length);
        }

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
        util.Log.debug("[NETWORK] GAME_START sent seq=" + seq);

    }

    public void addPeer(
            NodeId peerId,
            P2PConnection connection,
            PublicKey peerKey
    ) {
        util.Log.info("[NET] Peer added: " + peerId);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] Creating PeerSession peer=" + peerId +
                    " knownPeers=" + sessions.size());
        }
        PeerSession session =
                new PeerSession(
                        peerId,
                        connection,
                        serializer,
                        crypto,
                        peerKey,
                        packet -> handlePacket(peerId, packet),
                        this::handleInvalidSignature
                );

        sessions.put(peerId, session);
    }

    private void handlePacket(
            NodeId peer,
            NetworkPacket packet
    ) {
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] Handling packet peer=" + peer +
                    " type=" + packet.type() +
                    " tick=" + packet.tickNumber() +
                    " seq=" + packet.sequenceNumber() +
                    " payload=" + packet.payload().length);
        }
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

            case STATE_FRAME -> {

                if (isValidator && ruleValidator != null) {
                    StateFramePayload report = null;
                    RuleValidationEngine.ValidationResult result = null;
                    try {
                        report = StateFramePayload.fromBytes(packet.payload());
                    } catch (RuntimeException e) {
                        result = RuleValidationEngine.ValidationResult.invalid(
                                peer,
                                packet.tickNumber(),
                                "state report payload is malformed"
                        );
                    }

                    if (report != null && report.tick != packet.tickNumber()) {
                        result = RuleValidationEngine.ValidationResult.invalid(
                                peer,
                                packet.tickNumber(),
                                "state report tick " + report.tick + " does not match packet tick " + packet.tickNumber()
                        );
                    } else if (report != null) {
                        result = ruleValidator.acceptStateReport(peer, report);
                    }

                    if (result != null && !result.valid()) {
                        reportRuleViolation(result);
                        sendVoid(result.tick());
                    }
                }
            }
            case CHARACTER_SELECT -> {
                CharacterSelectPayload payload = CharacterSelectPayload.fromBytes(packet.payload());

                if (payload.playerId != packet.sender().value()) {
                    util.Log.warn("[NET] rejected character selection sender=" + packet.sender()
                            + " claimedPlayer=" + payload.playerId);
                    return;
                }

                selectedCharacters.put(payload.playerId, payload.characterId);

                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[NET] character selected player=" + payload.playerId
                            + " character=" + payload.characterId
                            + " selections=" + selectedCharacters);
                }
            }
            case GAME_START -> {
                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[GAME][NET] GAME_START received from=" + peer +
                            " tick=" + packet.tickNumber());
                }
                handleGameStart(packet);
            }

            case VOID -> handleVoid(packet);
        }
    }

    public void submitLocalInput(int tick, byte[] input) {

        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] submitLocalInput tick=" + tick +
                    " bytes=" + input.length +
                    " peers=" + sessions.size());
        }
        lockstep.submitLocalInput(tick, input);

        NetworkPacket packet =
                createPacket(
                        NetworkPacket.PacketType.INPUT,
                        tick,
                        input
                );

        broadcast(packet);
    }

    public void submitStateFrame(int tick, WorldState world) {

        StateFramePayload payload = StateFramePayload.fromWorld(world);
        byte[] payloadBytes = payload.toBytes();
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[VALIDATOR][NET] submitStateFrame tick=" + tick +
                    " players=" + payload.players.size() +
                    " projectiles=" + payload.projectiles.size() +
                    " gameOver=" + payload.gameOver +
                    " winner=" + payload.winnerPlayerId);
        }

        NetworkPacket packet =
                createPacket(
                        NetworkPacket.PacketType.STATE_FRAME,
                        tick,
                        payloadBytes
                );

        broadcast(packet);
    }

    public void submitCharacterSelection(long playerId, int characterId) {
        selectedCharacters.put(playerId, characterId);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] submitCharacterSelection player=" + playerId +
                    " character=" + characterId +
                    " selections=" + selectedCharacters);
        }

        CharacterSelectPayload payload = new CharacterSelectPayload(playerId, characterId);
        NetworkPacket packet =
                createPacket(
                        NetworkPacket.PacketType.CHARACTER_SELECT,
                        0,
                        payload.toBytes()
                );

        broadcast(packet);
    }

    public int getSelectedCharacterId(long playerId) {
        return selectedCharacters.getOrDefault(playerId, 1);
    }

    public void configureRuleValidator(
            WorldState initialWorld,
            List<GameSystem> gameSystems,
            ProjectileRegistry projectileRegistry
    ) {
        this.ruleValidator = new RuleValidationEngine(initialWorld, gameSystems, projectileRegistry);
    }

    private NetworkPacket createPacket(
            NetworkPacket.PacketType type,
            int tick,
            byte[] payload
    ) {

        byte[] payloadCopy = payload.clone();
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] createPacket type=" + type +
                    " tick=" + tick +
                    " payload=" + payloadCopy.length +
                    " seq=" + sequenceCounter);
        }

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
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] broadcast type=" + packet.type() +
                    " tick=" + packet.tickNumber() +
                    " seq=" + packet.sequenceNumber() +
                    " peers=" + sessions.size());
        }
        for (var it = sessions.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            try {
                entry.getValue().sendPacket(packet);
            } catch (RuntimeException e) {
                util.Log.warn("[NET] dropping peer " + entry.getKey() + " after send failure: " + e.getMessage());
                try {
                    entry.getValue().close();
                } catch (Exception ignored) {
                }
                it.remove();
            }
        }
    }

    public Map<NodeId, byte[]> waitForInputs(int tick) {
        return lockstep.waitForInputs(tick);
    }

    private volatile UUID currentGameId;
    private final VDFModule vdf = new VDFModule();

    private boolean isValidator;

    private void handleGameStart(NetworkPacket packet) {

        GameStartPayload payload = GameStartPayload.fromBytes(packet.payload());

        if (currentGameId != null)
            return;
        currentGameId = payload.gameId;
        util.Log.info("[GAME][NET] start " + payload.gameId);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[GAME][NET] handleGameStart playerA=" + payload.playerA +
                    " playerB=" + payload.playerB +
                    " currentGameId=" + currentGameId);
        }

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
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[CONSENSUS] VDF start gameId=" + payload.gameId +
                    " nodes=" + nodeIds);
        }

        byte[] seed = VDFSeed.create(
                payload.gameId,
                nodeIds
        );

        byte[] entropy = vdf.compute(seed);

        util.Log.debug("[VDF][NET] entropy=" + bytesToHex(entropy));
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[VDF][NET] seedBytes=" + seed.length + " entropyBytes=" + entropy.length);
        }

        long validator = 3L;

        NodeId validatorNodeId = new NodeId(validator);

        isValidator = validatorNodeId.equals(localNodeId);

        util.Log.info("[CONSENSUS] validator=" + validatorNodeId);

        if (isValidator) {
            util.Log.info("[ROLE] this node is VALIDATOR");
        } else {
            util.Log.info("[ROLE] this node is PLAYER");
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

            util.Log.warn("[VALIDATOR] invalid move tick=" + tick);

            sendVoid(tick);

        } else {
            if (ruleValidator != null) {
                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[VALIDATOR] validating input player=" + player +
                            " tick=" + tick +
                            " payload=" + packet.payload().length);
                }
                RuleValidationEngine.ValidationResult result = ruleValidator.acceptInput(packet);
                if (!result.valid()) {
                    reportRuleViolation(result);
                    sendVoid(tick);
                    return;
                }
            }
            util.Log.debug("[VALIDATOR] accepted move tick=" + tick);
        }
    }

    private void reportRuleViolation(RuleValidationEngine.ValidationResult result) {
        util.Log.warn("[VALIDATOR] desync culprit=" + result.culprit()
                + " tick=" + result.tick()
                + " reason=" + result.reason());
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[VALIDATOR] violation detail culprit=" + result.culprit() +
                    " tick=" + result.tick() +
                    " reason=" + result.reason());
        }
    }

    private void handleInvalidSignature(NodeId peer, NetworkPacket packet) {
        if (isValidator) {
            util.Log.warn("[VALIDATOR] invalid signature culprit=" + peer
                    + " claimedSender=" + packet.sender()
                    + " tick=" + packet.tickNumber()
                    + " type=" + packet.type());
            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[VALIDATOR] invalid signature payload=" + packet.payload().length +
                        " signature=" + (packet.signature() == null ? 0 : packet.signature().length));
            }
            sendVoid(packet.tickNumber());
        } else {
            util.Log.warn("[NET] invalid signature from=" + peer
                    + " claimedSender=" + packet.sender()
                    + " tick=" + packet.tickNumber()
                    + " type=" + packet.type());
            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[NET] invalid signature payload=" + packet.payload().length +
                        " signature=" + (packet.signature() == null ? 0 : packet.signature().length));
            }
        }
    }

    private void sendVoid(int tick) {
        if (currentGameId == null) {
            util.Log.warn("[VALIDATOR] void requested before game id is known tick=" + tick);
            return;
        }
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[VALIDATOR] sendVoid tick=" + tick + " gameId=" + currentGameId);
        }

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

        util.Log.warn("[GAME] VOID detected at tick " + payload.tick);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[GAME] VOID packet sender=" + packet.sender() +
                    " seq=" + packet.sequenceNumber() +
                    " gameId=" + payload.gameId +
                    " tick=" + payload.tick);
        }

        // Остановить игру
    }
}
