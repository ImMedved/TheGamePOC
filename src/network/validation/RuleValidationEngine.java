package network.validation;

import core.CommandCollector;
import core.CommandProcessor;
import core.SimulationContext;
import core.commands.Command;
import core.registries.ProjectileRegistry;
import core.states.WorldState;
import core.systems.GameSystem;
import core.systems.Phase;
import input.InputFrame;
import input.InputSnapshot;
import network.adapter.InputCodec;
import network.model.NetworkPacket;
import network.model.NodeId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class RuleValidationEngine {

    private static final float FLOAT_TOLERANCE_RATIO = 0.05f;
    private static final float DT = 1f / 30f;

    private final List<GameSystem> gameSystems;
    private final List<GameSystem> parallelGameSystems;
    private final List<GameSystem> sequentialGameSystems;
    private final CommandProcessor processor;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final InputCodec inputCodec = new InputCodec();

    private final Map<Integer, Map<Long, InputSnapshot>> inputsByTick = new HashMap<>();
    private final Map<Integer, WorldState> expectedByTick = new HashMap<>();
    private final Map<Integer, Map<NodeId, StateFramePayload>> reportsByTick = new HashMap<>();
    private final List<ValidationResult> pendingViolations = new ArrayList<>();

    private WorldState expectedWorld;
    private int nextTick = 0;

    public RuleValidationEngine(
            WorldState initialWorld,
            List<GameSystem> gameSystems,
            ProjectileRegistry projectileRegistry
    ) {
        this.expectedWorld = initialWorld.copy();
        this.gameSystems = gameSystems;
        this.parallelGameSystems = gameSystems.stream()
                .filter(s -> s.phase() == Phase.PARALLEL)
                .toList();
        this.sequentialGameSystems = gameSystems.stream()
                .filter(s -> s.phase() == Phase.SEQUENTIAL)
                .toList();
        this.processor = new CommandProcessor(projectileRegistry);
        expectedByTick.put(0, expectedWorld.copy());
    }

    public synchronized ValidationResult acceptInput(NetworkPacket packet) {
        InputSnapshot input;
        try {
            input = inputCodec.decode(packet.payload());
        } catch (RuntimeException e) {
            return ValidationResult.invalid(packet.sender(), packet.tickNumber(), "input payload is malformed");
        }

        ValidationResult sanity = validateInput(packet, input);
        if (!sanity.valid()) {
            return sanity;
        }

        inputsByTick
                .computeIfAbsent(packet.tickNumber(), ignored -> new HashMap<>())
                .put(packet.sender().value(), input);

        simulateReadyTicks();
        if (!pendingViolations.isEmpty()) {
            return pendingViolations.removeFirst();
        }
        return ValidationResult.valid(packet.sender(), packet.tickNumber());
    }

    public synchronized ValidationResult acceptStateReport(NodeId sender, StateFramePayload report) {
        reportsByTick
                .computeIfAbsent(report.tick, ignored -> new HashMap<>())
                .put(sender, report);

        return compareReport(sender, report);
    }

    private ValidationResult validateInput(NetworkPacket packet, InputSnapshot input) {
        long sender = packet.sender().value();
        int tick = packet.tickNumber();

        if (input.ownerId != sender) {
            return ValidationResult.invalid(packet.sender(), tick,
                    "sender " + sender + " tried to submit input for player " + input.ownerId);
        }

        if (input.tick != tick) {
            return ValidationResult.invalid(packet.sender(), tick,
                    "payload tick " + input.tick + " does not match packet tick " + tick);
        }

        if (!isFinite(input.moveX) || !isFinite(input.moveY)
                || !isFinite(input.mouseX) || !isFinite(input.mouseY)) {
            return ValidationResult.invalid(packet.sender(), tick, "input contains non-finite float values");
        }

        float movementLengthSq = input.moveX * input.moveX + input.moveY * input.moveY;
        if (movementLengthSq > 1.1025f) {
            return ValidationResult.invalid(packet.sender(), tick,
                    "movement vector is too long: " + Math.sqrt(movementLengthSq));
        }

        if (sender != 1L && sender != 2L) {
            return ValidationResult.invalid(packet.sender(), tick,
                    "only player nodes may submit gameplay input");
        }

        return ValidationResult.valid(packet.sender(), tick);
    }

    private void simulateReadyTicks() {
        while (true) {
            Map<Long, InputSnapshot> inputs = inputsByTick.get(nextTick);
            if (inputs == null || !inputs.containsKey(1L) || !inputs.containsKey(2L)) {
                return;
            }

            InputFrame frame = new InputFrame(nextTick);
            inputs.values().stream()
                    .sorted(Comparator.comparingLong(i -> i.ownerId))
                    .forEach(frame::put);

            expectedWorld = simulateOneTick(expectedWorld, frame);
            int producedTick = Math.toIntExact(expectedWorld.tickIndex);
            expectedByTick.put(producedTick, expectedWorld.copy());
            inputsByTick.remove(nextTick);
            pruneOldTicks(producedTick);

            Map<NodeId, StateFramePayload> reports = reportsByTick.get(producedTick);
            if (reports != null) {
                for (Map.Entry<NodeId, StateFramePayload> entry : reports.entrySet()) {
                    ValidationResult result = compareReport(entry.getKey(), entry.getValue());
                    if (!result.valid()) {
                        pendingViolations.add(result);
                    }
                }
            }

            nextTick++;
        }
    }

    private WorldState simulateOneTick(WorldState snapshot, InputFrame frame) {
        List<List<Command>> phaseALists = new ArrayList<>();

        for (GameSystem gameSystem : parallelGameSystems) {
            List<Command> localList = new ArrayList<>();
            phaseALists.add(localList);
            gameSystem.update(new SimulationContext(
                    snapshot,
                    frame,
                    DT,
                    idGenerator::getAndIncrement,
                    localList,
                    1L
            ));
        }

        List<Command> phaseACommands = CommandCollector.merge(phaseALists);
        WorldState provisional = processor.apply(snapshot, phaseACommands);
        List<List<Command>> phaseBLists = new ArrayList<>();

        for (GameSystem gameSystem : sequentialGameSystems) {
            List<Command> localList = new ArrayList<>();
            phaseBLists.add(localList);
            gameSystem.update(new SimulationContext(
                    provisional,
                    frame,
                    DT,
                    idGenerator::getAndIncrement,
                    localList,
                    1L
            ));
        }

        List<Command> all = new ArrayList<>(phaseACommands);
        all.addAll(CommandCollector.merge(phaseBLists));
        return processor.apply(snapshot, all);
    }

    private ValidationResult compareReport(NodeId sender, StateFramePayload report) {
        WorldState expected = expectedByTick.get(report.tick);
        if (expected == null) {
            return ValidationResult.valid(sender, report.tick);
        }

        StateFramePayload expectedReport = StateFramePayload.fromWorld(expected);
        String mismatch = compare(expectedReport, report);
        if (mismatch != null) {
            return ValidationResult.invalid(sender, report.tick, mismatch);
        }

        return ValidationResult.valid(sender, report.tick);
    }

    private String compare(StateFramePayload expected, StateFramePayload actual) {
        if (expected.gameOver != actual.gameOver) {
            return "gameOver mismatch expected=" + expected.gameOver + " actual=" + actual.gameOver;
        }

        if (expected.winnerPlayerId != actual.winnerPlayerId) {
            return "winner mismatch expected=" + expected.winnerPlayerId + " actual=" + actual.winnerPlayerId;
        }

        if (expected.players.size() != actual.players.size()) {
            return "player count mismatch expected=" + expected.players.size() + " actual=" + actual.players.size();
        }

        for (int i = 0; i < expected.players.size(); i++) {
            StateFramePayload.PlayerFrame e = expected.players.get(i);
            StateFramePayload.PlayerFrame a = actual.players.get(i);

            if (e.id() != a.id()) {
                return "player id mismatch expected=" + e.id() + " actual=" + a.id();
            }
            if (!near(e.x(), a.x())) {
                return "player " + e.id() + " x mismatch expected=" + e.x() + " actual=" + a.x();
            }
            if (!near(e.y(), a.y())) {
                return "player " + e.id() + " y mismatch expected=" + e.y() + " actual=" + a.y();
            }
            if (!near(e.health(), a.health())) {
                return "player " + e.id() + " health mismatch expected=" + e.health() + " actual=" + a.health();
            }
            if (e.alive() != a.alive()) {
                return "player " + e.id() + " alive mismatch expected=" + e.alive() + " actual=" + a.alive();
            }
            if (e.livesRemaining() != a.livesRemaining()) {
                return "player " + e.id() + " lives mismatch expected=" + e.livesRemaining()
                        + " actual=" + a.livesRemaining();
            }
        }

        if (expected.projectiles.size() != actual.projectiles.size()) {
            return "projectile count mismatch expected=" + expected.projectiles.size()
                    + " actual=" + actual.projectiles.size();
        }

        for (int i = 0; i < expected.projectiles.size(); i++) {
            StateFramePayload.ProjectileFrame e = expected.projectiles.get(i);
            StateFramePayload.ProjectileFrame a = actual.projectiles.get(i);

            if (e.id() != a.id()) {
                return "projectile id mismatch expected=" + e.id() + " actual=" + a.id();
            }
            if (e.ownerId() != a.ownerId()) {
                return "projectile " + e.id() + " owner mismatch expected=" + e.ownerId() + " actual=" + a.ownerId();
            }
            if (!near(e.x(), a.x())) {
                return "projectile " + e.id() + " x mismatch expected=" + e.x() + " actual=" + a.x();
            }
            if (!near(e.y(), a.y())) {
                return "projectile " + e.id() + " y mismatch expected=" + e.y() + " actual=" + a.y();
            }
        }

        return null;
    }

    private void pruneOldTicks(int currentTick) {
        int cutoff = currentTick - 180;
        expectedByTick.keySet().removeIf(tick -> tick < cutoff);
        reportsByTick.keySet().removeIf(tick -> tick < cutoff);
        inputsByTick.keySet().removeIf(tick -> tick < cutoff);
    }

    private boolean near(float expected, float actual) {
        float tolerance = Math.max(0.001f, Math.max(Math.abs(expected), 1f) * FLOAT_TOLERANCE_RATIO);
        return Math.abs(expected - actual) <= tolerance;
    }

    private boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    public record ValidationResult(boolean valid, NodeId culprit, int tick, String reason) {
        public static ValidationResult valid(NodeId node, int tick) {
            return new ValidationResult(true, node, tick, "");
        }

        public static ValidationResult invalid(NodeId node, int tick, String reason) {
            return new ValidationResult(false, node, tick, reason);
        }
    }
}
