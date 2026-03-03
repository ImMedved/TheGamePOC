package render.renderers;

import core.render.RenderSnapshot;
import input.InputModule;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.window.VideoMode;
import org.jsfml.window.WindowStyle;
import render.Camera;
import render.batch.BatchManager;
import render.resources.ResourceManager;

public final class SceneRenderer {

    private final ResourceManager resources;
    private final InputModule inputModule;

    private RenderWindow window;

    private final Camera camera = new Camera();
    private final BatchManager batchManager = new BatchManager();

    private BackgroundRenderer backgroundRenderer;
    private LevelRenderer levelRenderer;
    private PlayerRenderer playerRenderer;
    private ProjectileRenderer projectileRenderer;
    private EffectRenderer effectRenderer;

    private boolean levelInitialized = false;

    public SceneRenderer(ResourceManager resources, InputModule inputModule) {
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

        backgroundRenderer = new BackgroundRenderer(resources);
        levelRenderer = new LevelRenderer(resources);
        playerRenderer = new PlayerRenderer(resources);
        projectileRenderer = new ProjectileRenderer(resources);
        effectRenderer = new EffectRenderer(resources);

        playerRenderer.init();
        projectileRenderer.init();
        effectRenderer.init();
    }

    public void render(RenderSnapshot snapshot, float alpha) {

        window.clear(Color.BLACK);

        if (snapshot == null) {
            window.display();
            return;
        }

        if (!levelInitialized && snapshot.level != null) {
            levelRenderer.init(snapshot.level);
            levelInitialized = true;
        }

        if (!snapshot.players.isEmpty()) {
            //System.out.println("!snapshot.players.isEmpty() trigger");
            var focus = snapshot.players.get(0);

            float x = focus.prevX + (focus.currX - focus.prevX) * alpha;
            float y = focus.prevY + (focus.currY - focus.prevY) * alpha;

            camera.setPosition(snapshot.camX, snapshot.camY);
        }

        batchManager.beginFrame();

        backgroundRenderer.render(camera, window);
        levelRenderer.render(camera, window);

        playerRenderer.render(snapshot.players, camera, alpha, batchManager);
        projectileRenderer.render(snapshot.projectiles, camera, alpha, batchManager);
        effectRenderer.render(snapshot.effects, camera, batchManager);

        for (var entry : batchManager.getAll().entrySet()) {
            window.draw(entry.getValue().getVertexArray(), entry.getKey().getStates());
        }
        //System.out.println("batchManager size: " + batchManager.getAll().size());
        window.display();
    }

    public RenderWindow getWindow() {
        return window;
    }

    public void shutdown() {
        window.close();
    }
}