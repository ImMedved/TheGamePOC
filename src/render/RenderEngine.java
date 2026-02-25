package render;

import core.CoreEngine;
import core.render.RenderSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RenderEngine {

    private static final int TARGET_FPS = 120;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;

    private final CoreEngine core;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread renderThread;

    private SceneRenderer sceneRenderer;

    private long previousTime;
    private double accumulator = 0.0;

    public RenderEngine(CoreEngine core) {
        this.core = core;
    }

    public void start() {
        if (running.get()) return;

        running.set(true);
        renderThread = new Thread(this::runLoop, "RenderThread");
        renderThread.start();
    }

    public void stop() {
        running.set(false);
    }

    private void runLoop() {

        init();

        previousTime = java.lang.System.nanoTime();

        while (running.get()) {

            long now = java.lang.System.nanoTime();
            double elapsed =
                    (now - previousTime) / 1_000_000_000.0;

            previousTime = now;
            accumulator += elapsed;

            while (accumulator >= FRAME_TIME) {
                accumulator -= FRAME_TIME;
            }

            double alpha = accumulator / FRAME_TIME;

            renderFrame((float) alpha);
        }

        shutdown();
    }

    private void init() {
        sceneRenderer = new SceneRenderer();
        sceneRenderer.init();
    }

    private void renderFrame(float alpha) {

        RenderSnapshot snapshot =
                core.getRenderSnapshot();

        if (snapshot == null)
            return;

        sceneRenderer.render(snapshot, alpha);
    }

    private void shutdown() {
        sceneRenderer.shutdown();
    }
}