package core;

import core.commands.Command;
import core.states.WorldState;
import core.systems.*;
import core.systems.System;
import input.InputModule;
import input.InputSnapshot;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class CoreEngine {

    private static final int TICK_RATE = 60;
    private static final float DT = 1f / TICK_RATE;

    private final ExecutorService executor;
    private final InputModule input;

    private final List<System> systems;

    private final CommandProcessor processor = new CommandProcessor();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private volatile boolean running = false;
    private WorldState world;

    public CoreEngine(InputModule input,
                      WorldState initial,
                      List<System> systems) {

        this.input = input;
        this.world = initial;
        this.systems = systems;

        this.executor =
                Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors()
                );
    }

    public void start() {
        running = true;
        new Thread(this::runLoop, "CoreLoop").start();
    }

    public void stop() {
        running = false;
        executor.shutdown();
    }

    public WorldState getLatestWorldState() {
        return world;
    }

    private void runLoop() {

        long previous = java.lang.System.nanoTime();
        double lag = 0.0;

        while (running) {

            long now = java.lang.System.nanoTime();
            double elapsed =
                    (now - previous) / 1_000_000_000.0;

            previous = now;
            lag += elapsed;

            while (lag >= DT) {
                tick();
                lag -= DT;
            }
        }
    }

    private void tick() {

        input.publishSnapshot((int) world.tickIndex);
        InputSnapshot inputSnapshot =
                input.getLatestSnapshot();

        WorldState snapshot = world;

        List<List<Command>> phaseALists = new ArrayList<>();

        List<System> parallelSystems = systems.stream()
                .filter(s -> s.phase() == Phase.PARALLEL)
                .toList();

        List<System> sequentialSystems = systems.stream()
                .filter(s -> s.phase() == Phase.SEQUENTIAL)
                .toList();

        List<Callable<Void>> tasks = new ArrayList<>();

        for (System system : parallelSystems) {

            List<Command> localList = new ArrayList<>();
            phaseALists.add(localList);

            SimulationContext ctx =
                    new SimulationContext(
                            snapshot,
                            inputSnapshot,
                            DT,
                            idGenerator::getAndIncrement,
                            localList
                    );

            tasks.add(() -> {
                system.update(ctx);
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException ignored) {
        }

        List<Command> phaseACommands =
                CommandCollector.merge(phaseALists);

        WorldState provisional =
                processor.apply(snapshot, phaseACommands);

        List<List<Command>> phaseBLists = new ArrayList<>();

        for (System system : sequentialSystems) {

            List<Command> localList = new ArrayList<>();
            phaseBLists.add(localList);

            SimulationContext ctx =
                    new SimulationContext(
                            provisional,
                            inputSnapshot,
                            DT,
                            idGenerator::getAndIncrement,
                            localList
                    );

            system.update(ctx);
        }

        List<Command> phaseBCommands =
                CommandCollector.merge(phaseBLists);

        List<Command> all =
                new ArrayList<>(phaseACommands);
        all.addAll(phaseBCommands);

        world = processor.apply(snapshot, all);
    }
}