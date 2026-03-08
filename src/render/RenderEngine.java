package render;

import core.CoreEngine;
import core.render.RenderSnapshot;
import input.InputModule;
import input.InputSnapshot;
import network.node.NetworkNode;
import network.adapter.NetworkInputProvider;
import org.jsfml.window.event.Event;
import render.renderers.MenuRenderer;
import render.renderers.SceneRenderer;
import render.resources.ResourceManager;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RenderEngine {

    private static final int TARGET_FPS = 60;
    private static final double FRAME_TIME = 1.0 / TARGET_FPS;

    private final CoreEngine core;
    private final ResourceManager resourceManager;
    private final InputModule inputModule;
    private final NetworkNode networkNode;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread renderThread;

    private SceneRenderer sceneRenderer;
    private MenuRenderer menuRenderer;

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

        previousTime = System.nanoTime();

        while (running.get()) {

            long now = System.nanoTime();
            double elapsed = (now - previousTime) / 1_000_000_000.0;

            previousTime = now;
            accumulator += elapsed;

            while (accumulator >= FRAME_TIME) {
                accumulator -= FRAME_TIME;
            }

            float alpha = (float)(accumulator / FRAME_TIME);
            renderFrame(alpha);
        }

        shutdown();
    }

    private void init() {

        sceneRenderer = new SceneRenderer(resourceManager, inputModule, localPlayerId);
        sceneRenderer.init();

        menuRenderer = new MenuRenderer(resourceManager, this::startGame);
        menuRenderer.init(sceneRenderer.getWindow());
    }

    private void startGame() {

        int selected = menuRenderer.getSelectedCharacterId();
        core.setSelectedCharacter(selected);

        NetworkInputProvider provider =
                new NetworkInputProvider(
                        networkNode,
                        inputModule,
                        localPlayerId,
                        remotePlayerId
                );

        core.start(provider);

        mode = AppMode.GAME;
    }

    private void renderFrame(float alpha) {
        var window = sceneRenderer.getWindow();

        for (Event event : window.pollEvents()) {

            inputModule.handleEvent(event);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }

            if (mode == AppMode.MENU) {
                menuRenderer.handleEvent(event);
            }
        }

        if (mode == AppMode.MENU) {
            menuRenderer.render(window);
            return;
        }

        RenderSnapshot snapshot = core.getRenderSnapshot();
        if (snapshot == null) return;

        sceneRenderer.render(snapshot, alpha);
    }

    private void shutdown() {
        core.stop();
        sceneRenderer.shutdown();
    }
}