package core;

import core.commands.Command;
import core.registries.ProjectileRegistry;
import core.render.RenderSnapshot;
import core.render.RenderSnapshotBuilder;
import core.states.WorldState;
import core.systems.*;
import core.systems.GameSystem;
import input.InputFrame;
import input.InputModule;
import input.InputSnapshot;
import network.adapter.WorldHasher;
import network.node.NetworkNode;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class CoreEngine {

    private static final int TICK_RATE = 60;
    private static final float DT = 1f / TICK_RATE;

    private final ExecutorService executor;

    private final List<GameSystem> gameSystems;

    private final CommandProcessor processor;
    private final AtomicLong idGenerator = new AtomicLong(1);

    private volatile boolean running = false;

    private WorldState previousWorld;
    private WorldState currentWorld;

    private final RenderSnapshotBuilder snapshotBuilder = new RenderSnapshotBuilder();

    private final java.util.concurrent.atomic.AtomicReference<RenderSnapshot> renderSnapshotRef =
            new java.util.concurrent.atomic.AtomicReference<>();

    private final WorldHasher worldHasher = new WorldHasher();
    private final NetworkNode networkNode;
    private static final int HASH_INTERVAL = 1;

    private volatile Integer pendingCharacterId = null;

    public void setSelectedCharacter(int characterId) {
        this.pendingCharacterId = characterId;
    }

    public CoreEngine(WorldState initial,
                      List<GameSystem> gameSystems,
                      ProjectileRegistry projectileRegistry,
                      NetworkNode networkNode) {

        this.previousWorld = initial;
        this.currentWorld = initial;
        this.gameSystems = gameSystems;
        this.processor = new CommandProcessor(projectileRegistry);
        this.networkNode = networkNode;

        RenderSnapshot first = snapshotBuilder.build(initial, initial);

        renderSnapshotRef.set(first);

        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void start(java.util.function.Supplier<InputFrame> inputSupplier) {

        running = true;

        new Thread(() -> runLoop(inputSupplier), "CoreLoop").start();
    }

    public void stop() {
        running = false;
        executor.shutdown();
    }

    private void runLoop(java.util.function.Supplier<InputFrame> inputSupplier) {

        long previous = java.lang.System.nanoTime();
        double lag = 0.0;
        System.out.println("[CORE] CoreLoop Thread started");
        while (running) {

            long now = java.lang.System.nanoTime();
            double elapsed = (now - previous) / 1_000_000_000.0;

            previous = now;
            lag += elapsed;

            while (lag >= DT) {

                InputFrame frame = inputSupplier.get();
                tick(frame);

                lag -= DT;
            }
        }
    }

    private void tick(InputFrame frame) {
        System.out.println("[CORE] Tick triggered");
        if (pendingCharacterId != null) {
            System.out.println("[CORE] Players count: " + currentWorld.players.size());
            for (var player : currentWorld.players.values()) {
                player.characterId = pendingCharacterId;
            }

            pendingCharacterId = null;
        }

        WorldState snapshot = currentWorld;

        List<List<Command>> phaseALists = new ArrayList<>();

        List<GameSystem> parallelGameSystems = gameSystems.stream()
                .filter(s -> s.phase() == Phase.PARALLEL)
                .toList();

        List<GameSystem> sequentialGameSystems = gameSystems.stream()
                .filter(s -> s.phase() == Phase.SEQUENTIAL)
                .toList();

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
                            localList
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
                            localList
                    );

            gameSystem.update(ctx);
        }

        List<Command> phaseBCommands = CommandCollector.merge(phaseBLists);

        List<Command> all = new ArrayList<>(phaseACommands);
        all.addAll(phaseBCommands);

        WorldState nextWorld = processor.apply(snapshot, all);

        int tickIndex = Math.toIntExact(nextWorld.tickIndex);

        if (networkNode != null && tickIndex % HASH_INTERVAL == 0) {

            byte[] hash = worldHasher.hash(nextWorld);

            networkNode.submitStateHash(
                    tickIndex,
                    hash
            );
        }

        RenderSnapshot snapshotRender = snapshotBuilder.build(previousWorld, nextWorld);
        renderSnapshotRef.set(snapshotRender);

        previousWorld = nextWorld;
        currentWorld = nextWorld;
    }

    public RenderSnapshot getRenderSnapshot() {
        return renderSnapshotRef.get();
    }
}