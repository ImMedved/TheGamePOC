package core;

import core.commands.Command;
import core.registries.ProjectileRegistry;
import core.render.RenderSnapshot;
import core.render.RenderSnapshotBuilder;
import core.states.WorldState;
import core.states.PlayerState;
import core.systems.*;
import core.systems.GameSystem;
import input.InputFrame;
import network.node.NetworkNode;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public final class CoreEngine {

    private static final int TICK_RATE = 25;
    private static final float DT = 1f / TICK_RATE;

    private final ExecutorService executor;

    private final List<GameSystem> parallelGameSystems;
    private final List<GameSystem> sequentialGameSystems;

    private final CommandProcessor processor;
    private final AtomicLong idGenerator = new AtomicLong(1);

    private volatile boolean running = false;

    private WorldState previousWorld;
    private WorldState currentWorld;

    private final RenderSnapshotBuilder snapshotBuilder = new RenderSnapshotBuilder();

    private final java.util.concurrent.atomic.AtomicReference<RenderSnapshot> renderSnapshotRef =
            new java.util.concurrent.atomic.AtomicReference<>();

    private final NetworkNode networkNode;
    private static final int STATE_REPORT_INTERVAL = 60;
    private final WorldState initialWorld;
    private int lastReportedStateTick = -1;

    private final Map<Long, Integer> pendingCharacterIds = new ConcurrentHashMap<>();

    public void setPlayerCharacter(long playerId, int characterId) {
        pendingCharacterIds.put(playerId, characterId);
    }

    public CoreEngine(WorldState initial,
                      List<GameSystem> gameSystems,
                      ProjectileRegistry projectileRegistry,
                      NetworkNode networkNode) {

        this.initialWorld = initial.copy();
        this.previousWorld = initial;
        this.currentWorld = initial;
        this.parallelGameSystems = gameSystems.stream().filter(s -> s.phase() == Phase.PARALLEL).toList();
        this.sequentialGameSystems = gameSystems.stream().filter(s -> s.phase() == Phase.SEQUENTIAL).toList();
        this.processor = new CommandProcessor(projectileRegistry);
        this.networkNode = networkNode;

        RenderSnapshot first = snapshotBuilder.build(initial, initial);

        renderSnapshotRef.set(first);

        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void start(java.util.function.Supplier<InputFrame> inputSupplier, long localPlayerId) {

        if (running) {
            return;
        }
        running = true;

        new Thread(() -> runLoop(inputSupplier, localPlayerId), "CoreLoop").start();
    }

    public void stop() {
        util.Log.info("[CORE] stop requested");
        running = false;
    }

    public void reset() {
        stop();
        idGenerator.set(1);
        lastReportedStateTick = -1;
        previousWorld = initialWorld.copy();
        currentWorld = initialWorld.copy();
        renderSnapshotRef.set(snapshotBuilder.build(previousWorld, currentWorld));
        pendingCharacterIds.clear();
    }

    private void runLoop(java.util.function.Supplier<InputFrame> inputSupplier, long localPlayerId) {

        long previous = java.lang.System.nanoTime();
        double lag = 0.0;
        util.Log.info("[CORE] loop started");
        while (running) {

            long now = java.lang.System.nanoTime();
            double elapsed = (now - previous) / 1_000_000_000.0;

            previous = now;
            lag = Math.min(lag + elapsed, DT * 5);

            while (lag >= DT) {

                InputFrame frame = inputSupplier.get();
                tick(frame, localPlayerId);

                lag -= DT;
            }

            if (lag < DT) {
                LockSupport.parkNanos(500_000L);
            }
        }
    }

    private void tick(InputFrame frame, long localPlayerId) {
        boolean debug = util.Log.isDebugEnabled();
        if (debug) {
            util.Log.debug("[CORE] Tick triggered");
        }
        long start = System.nanoTime();

        if (!pendingCharacterIds.isEmpty()) {
            util.Log.debug("[CORE] Players count=" + currentWorld.players.size());
            for (var entry : pendingCharacterIds.entrySet()) {
                PlayerState player = currentWorld.players.get(entry.getKey());
                if (player != null) {
                    player.characterId = entry.getValue();
                }
            }

            pendingCharacterIds.clear();
        }

        WorldState snapshot = currentWorld;

        if (debug) {
            for (var e : frame.all().entrySet()) {
                util.Log.debug("[CORE] frame input player=" + e.getKey()
                        + " moveX=" + e.getValue().moveX
                        + " tick=" + frame.tick);
            }
        }

        List<List<Command>> phaseALists = new ArrayList<>();

        List<Callable<Void>> tasks = new ArrayList<>();

        for (GameSystem gameSystem : parallelGameSystems) {

            List<Command> localList = new ArrayList<>();
            phaseALists.add(localList);

            SimulationContext ctx =
                    new SimulationContext(
                            snapshot,
                            frame,
                            DT,
                            idGenerator::getAndIncrement,
                            localList,
                            localPlayerId
                    );

            tasks.add(() -> {
                gameSystem.update(ctx);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException ignored) {
        }

        List<Command> phaseACommands = CommandCollector.merge(phaseALists);

        WorldState provisional = processor.apply(snapshot, phaseACommands);

        List<List<Command>> phaseBLists = new ArrayList<>();

        for (GameSystem gameSystem : sequentialGameSystems) {

            List<Command> localList = new ArrayList<>();
            phaseBLists.add(localList);

            SimulationContext ctx =
                    new SimulationContext(
                            provisional,
                            frame,
                            DT,
                            idGenerator::getAndIncrement,
                            localList,
                            localPlayerId
                    );

            gameSystem.update(ctx);
        }

        List<Command> phaseBCommands = CommandCollector.merge(phaseBLists);

        List<Command> all = new ArrayList<>(phaseACommands);
        all.addAll(phaseBCommands);

        WorldState nextWorld = processor.apply(snapshot, all);

        int tickIndex = Math.toIntExact(nextWorld.tickIndex);

        boolean shouldReportState = tickIndex % STATE_REPORT_INTERVAL == 0 || nextWorld.gameOver;
        if (networkNode != null && shouldReportState && tickIndex != lastReportedStateTick) {
            networkNode.submitStateFrame(tickIndex, nextWorld);
            lastReportedStateTick = tickIndex;
        }

        RenderSnapshot snapshotRender = snapshotBuilder.build(previousWorld, nextWorld);
        renderSnapshotRef.set(snapshotRender);

        previousWorld = nextWorld;
        currentWorld = nextWorld;

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;

        if (debug) util.Log.debug("[METRIC][CORE] tick time=" + ms + "ms");
    }

    public RenderSnapshot getRenderSnapshot() {
        return renderSnapshotRef.get();
    }

    public core.states.CameraState getCameraSnapshot() {
        return currentWorld.camera != null ? currentWorld.camera.copy() : null;
    }
}
