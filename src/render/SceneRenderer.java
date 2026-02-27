package render;

import core.render.RenderSnapshot;
import input.InputModule;
import org.jsfml.window.VideoMode;
import org.jsfml.window.event.Event;
import render.batch.BatchManager;
import render.resources.ResourceManager;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.window.WindowStyle;
import org.jsfml.graphics.Color;

public final class SceneRenderer {

    private final ResourceManager resources;
    private final InputModule inputModule;

    private RenderWindow window;

    private final Camera camera = new Camera();
    private final BatchManager batchManager = new BatchManager();

    private LevelRenderer levelRenderer;
    private PlayerRenderer playerRenderer;
    private ProjectileRenderer projectileRenderer;
    private EffectRenderer effectRenderer;

    public SceneRenderer(ResourceManager resources,
                         InputModule inputModule) {
        this.resources = resources;
        this.inputModule = inputModule;
    }

    public void init() {

        window = new RenderWindow(
                new VideoMode(1920, 1080),
                "Game",
                WindowStyle.DEFAULT
        );

        window.setFramerateLimit(0);
        camera.setViewport(1920f, 1080f);

        levelRenderer = new LevelRenderer(resources);
        playerRenderer = new PlayerRenderer(resources);
        projectileRenderer = new ProjectileRenderer(resources);
        effectRenderer = new EffectRenderer(resources);

        levelRenderer.init();
        playerRenderer.init();
        projectileRenderer.init();
        effectRenderer.init();
    }

    public void render(RenderSnapshot snapshot, float alpha) {

        for (Event event : window.pollEvents()) {

            inputModule.handleEvent(event);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }
        }

        window.clear(Color.BLACK);
        batchManager.beginFrame();

        if (!snapshot.players.isEmpty()) {
            var focus = snapshot.players.get(0);

            float x = focus.prevX + (focus.currX - focus.prevX) * alpha;
            float y = focus.prevY + (focus.currY - focus.prevY) * alpha;

            camera.update(x, y, snapshot.level);
        }

        levelRenderer.render(snapshot.level, camera, batchManager.level());
        playerRenderer.render(snapshot.players, camera, alpha, batchManager.players());
        projectileRenderer.render(snapshot.projectiles, camera, alpha, batchManager.projectiles());
        effectRenderer.render(snapshot.effects, camera, batchManager.effects());

        window.draw(batchManager.level().getVertexArray(), levelRenderer.getStates());
        window.draw(batchManager.players().getVertexArray(), playerRenderer.getStates());
        window.draw(batchManager.projectiles().getVertexArray(), projectileRenderer.getStates());
        window.draw(batchManager.effects().getVertexArray(), effectRenderer.getStates());

        window.display();
    }

    public void shutdown() {
        window.close();
    }
}