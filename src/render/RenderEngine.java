package render;

import core.CoreEngine;
import core.render.RenderSnapshot;
import input.InputModule;
import input.InputSnapshot;
import network.node.NetworkNode;
import network.adapter.NetworkInputProvider;
import org.jsfml.window.event.Event;
import render.renderers.MenuRenderer;
import render.renderers.EndScreenRenderer;
import render.renderers.SceneRenderer;
import render.resources.ResourceManager;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public final class RenderEngine {

    private static final int TARGET_FPS = 30;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;
    private static final long FRAME_TIME_NANOS = 1_000_000_000L / TARGET_FPS;

    private final CoreEngine core;
    private final ResourceManager resourceManager;
    private final InputModule inputModule;
    private final NetworkNode networkNode;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread renderThread;

    private SceneRenderer sceneRenderer;
    private MenuRenderer menuRenderer;
    private EndScreenRenderer endScreenRenderer;
    private boolean localWon = false;

    private AppMode mode = AppMode.MENU;

    private long previousTime;
    private double accumulator = 0.0;

    private final long localPlayerId;
    private final long remotePlayerId;

    public RenderEngine(CoreEngine core,
                        ResourceManager resourceManager,
                        InputModule inputModule,
                        NetworkNode networkNode,
                        long localPlayerId,
                        long remotePlayerId) {

        this.core = core;
        this.resourceManager = resourceManager;
        this.inputModule = inputModule;
        this.networkNode = networkNode;
        this.localPlayerId = localPlayerId;
        this.remotePlayerId = remotePlayerId;
    }

    public void start() {
        util.Log.info("[RENDER] start requested running=" + running.get());
        if (running.get()) return;
        running.set(true);
        renderThread = new Thread(this::runLoop, "RenderThread");
        renderThread.start();
    }

    public void stop() {
        util.Log.info("[RENDER] stop requested");
        running.set(false);
    }

    private void runLoop() {
        util.Log.info("[RENDER] run loop started");
        init();

        previousTime = System.nanoTime();

        while (running.get()) {

            long frameStart = System.nanoTime();
            long now = System.nanoTime();
            double elapsed = (now - previousTime) / 1_000_000_000.0;

            previousTime = now;
            accumulator += elapsed;

            while (accumulator >= FRAME_TIME) {
                accumulator -= FRAME_TIME;
            }

            float alpha = (float)(accumulator / FRAME_TIME);
            renderFrame(alpha);

            long remaining = FRAME_TIME_NANOS - (System.nanoTime() - frameStart);
            if (remaining > 0) {
                LockSupport.parkNanos(remaining);
            }
        }

        shutdown();
    }

    private void init() {

        sceneRenderer = new SceneRenderer(resourceManager, inputModule, localPlayerId);
        sceneRenderer.init();

        menuRenderer = new MenuRenderer(resourceManager, this::startGame);
        menuRenderer.init(sceneRenderer.getWindow());
        endScreenRenderer = new EndScreenRenderer(resourceManager);
    }

    private void startGame() {

        int selected = menuRenderer.getSelectedCharacterId();
        core.reset();
        core.setSelectedCharacter(selected);

        NetworkInputProvider provider =
                new NetworkInputProvider(
                        networkNode,
                        inputModule,
                        localPlayerId,
                        remotePlayerId,
                        core::getCameraSnapshot
                );
        util.Log.info("[RENDER] game requested for localPlayer=" + localPlayerId);
        core.start(provider, localPlayerId);

        mode = AppMode.GAME;
    }

    private void renderFrame(float alpha) {
        long start = System.nanoTime();
        var window = sceneRenderer.getWindow();

        for (Event event : window.pollEvents()) {

            inputModule.handleEvent(event);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }

            if (mode == AppMode.MENU) {
                menuRenderer.handleEvent(event);
            } else if (mode == AppMode.END_SCREEN && isAnyButtonPress(event)) {
                core.reset();
                mode = AppMode.MENU;
            }
        }

        if (mode == AppMode.MENU) {
            menuRenderer.render(window);
            return;
        }

        if (mode == AppMode.END_SCREEN) {
            endScreenRenderer.render(window, localWon);
            return;
        }

        RenderSnapshot snapshot = core.getRenderSnapshot();
        if (snapshot == null) return;

        sceneRenderer.render(snapshot, alpha);

        if (snapshot.gameOver) {
            localWon = snapshot.winnerPlayerId == localPlayerId;
            core.stop();
            mode = AppMode.END_SCREEN;
        }

        long end = System.nanoTime();
        double ms = (end - start) / 1_000_000.0;
        util.Log.debug("[METRIC][RENDER] frame=" + ms + "ms");
    }

    private boolean isAnyButtonPress(Event event) {
        return event.type == Event.Type.KEY_PRESSED
                || event.type == Event.Type.MOUSE_BUTTON_PRESSED;
    }

    private void shutdown() {
        util.Log.info("[RENDER] shutting down");
        core.stop();
        sceneRenderer.shutdown();
    }
}
