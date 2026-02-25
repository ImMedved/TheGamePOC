package core;

import core.states.SimulationContext;
import core.commands.CommandBuffer;
import core.states.WorldState;
import core.systems.*;

import input.InputModule;
import input.InputSnapshot;

import java.util.concurrent.*;

public final class CoreEngine {

    private static final int TICK_RATE = 60;
    private static final float DT = 1f / TICK_RATE;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private final InputModule input;
    private final CommandProcessor processor =
            new CommandProcessor();

    private volatile boolean running;

    private WorldState world;

    private long idCounter = 0;

    private final MovementSystem movement =
            new MovementSystem();

    private final ProjectileSystem projectile =
            new ProjectileSystem();

    private final CollisionSystem collision =
            new CollisionSystem();

    public CoreEngine(InputModule input,
                      WorldState initialWorld) {

        this.input = input;
        this.world = initialWorld;
    }

    public void start() {
        running = true;
        new Thread(this::loop).start();
    }

    private void loop() {

        long previous = System.nanoTime();
        double lag = 0;

        while (running) {

            long now = System.nanoTime();
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

        input.publishSnapshot(0);
        InputSnapshot snapshot =
                input.getLatestSnapshot();

        CommandBuffer buffer =
                new CommandBuffer();

        SimulationContext context =
                new SimulationContext(
                        world,
                        buffer,
                        snapshot,
                        DT,
                        () -> idCounter++
                );

        try {

            executor.invokeAll(
                    java.util.List.of(
                            () -> { movement.update(context); return null; },
                            () -> { projectile.update(context); return null; }
                    )
            );

        } catch (InterruptedException ignored) {
        }

        collision.update(context);

        world = processor.apply(world, buffer);
    }
}