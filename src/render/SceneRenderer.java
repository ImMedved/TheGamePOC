package render;

import core.render.RenderSnapshot;
import org.jsfml.window.VideoMode;
import render.batch.BatchManager;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.window.WindowStyle;
import org.jsfml.graphics.Color;

public final class SceneRenderer {

    private RenderWindow window;

    private final Camera camera = new Camera();
    private final BatchManager batchManager = new BatchManager();

    private final LevelRenderer levelRenderer = new LevelRenderer();
    private final PlayerRenderer playerRenderer = new PlayerRenderer();
    private final ProjectileRenderer projectileRenderer = new ProjectileRenderer();
    private final EffectRenderer effectRenderer = new EffectRenderer();

    public void init() {

        window = new RenderWindow(
                new VideoMode(1280, 720),
                "Game",
                WindowStyle.DEFAULT
        );

        window.setFramerateLimit(0);

        camera.setViewport(1280f, 720f);

        levelRenderer.init();
        playerRenderer.init();
        projectileRenderer.init();
        effectRenderer.init();
    }

    public void render(RenderSnapshot snapshot, float alpha) {

        window.clear(Color.BLACK);

        batchManager.beginFrame();

        if (!snapshot.players.isEmpty()) {

            var focus = snapshot.players.get(0);

            float x = focus.prevX + (focus.currX - focus.prevX) * alpha;
            float y = focus.prevY + (focus.currY - focus.prevY) * alpha;

            camera.update(x, y, snapshot.level);
        }

        levelRenderer.render(
                snapshot.level,
                camera,
                batchManager.level()
        );

        playerRenderer.render(
                snapshot.players,
                camera,
                alpha,
                batchManager.players()
        );

        projectileRenderer.render(
                snapshot.projectiles,
                camera,
                alpha,
                batchManager.projectiles()
        );

        effectRenderer.render(
                snapshot.effects,
                camera,
                batchManager.effects()
        );

        window.draw(batchManager.level().getVertexArray());
        window.draw(batchManager.players().getVertexArray());
        window.draw(batchManager.projectiles().getVertexArray());
        window.draw(batchManager.effects().getVertexArray());

        window.display();
    }

    public void shutdown() {
        window.close();
    }
}