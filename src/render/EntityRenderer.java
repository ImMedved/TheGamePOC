package render;

import core.PlayerState;
import core.ProjectileState;
import core.WorldState;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.nio.file.Paths;

public class EntityRenderer {

    /*private final CircleShape localPlayerShape;
    private final CircleShape remotePlayerShape;*/
    private final CircleShape projectileShape;
    private final ProjectileBatchRenderer projectileBatch;

    private final Texture characterAtlas;
    private final AnimatedCharacter localCharacter;
    private final AnimatedCharacter remoteCharacter;
    private final CharacterBatchRenderer characterBatch;

    public EntityRenderer() {

        /*localPlayerShape = new CircleShape();
        localPlayerShape.setFillColor(Color.GREEN);

        remotePlayerShape = new CircleShape();
        remotePlayerShape.setFillColor(Color.RED);*/

        projectileShape = new CircleShape(4f);
        projectileShape.setFillColor(Color.WHITE);

        characterAtlas = new Texture();
        characterBatch = new CharacterBatchRenderer();

        projectileBatch = new ProjectileBatchRenderer();

        try {
            characterAtlas.loadFromFile(Paths.get("assets/chars.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int randomLocal = (int)(Math.random() * 6);
        int randomRemote = (int)(Math.random() * 6);

        localCharacter = new AnimatedCharacter(characterAtlas, randomLocal);
        remoteCharacter = new AnimatedCharacter(characterAtlas, randomRemote);
    }

    public void drawEntities(RenderWindow window, WorldState state) {

        float dt = 1f / 60f; // временно фикс, можно позже синхронизировать с render dt

        characterBatch.draw(window, state, dt);

        projectileBatch.draw(window,
                state.projectiles,
                state.bulletHoles,
                dt);
    }

    private void drawPlayer(RenderWindow window,
                            PlayerState player,
                            CircleShape shape) {

        shape.setRadius(player.hitboxRadius);
        shape.setOrigin(player.hitboxRadius, player.hitboxRadius);
        shape.setPosition(player.x, player.y);

        window.draw(shape);
    }

    private void drawProjectiles(RenderWindow window,
                                 java.util.List<ProjectileState> projectiles) {

        for (ProjectileState p : projectiles) {
            projectileShape.setPosition(p.x, p.y);
            window.draw(projectileShape);
        }
    }

    public void drawGameOver(RenderWindow window) {

        RectangleShape overlay = new RectangleShape(
                new Vector2f(window.getSize().x,
                        window.getSize().y)
        );

        overlay.setFillColor(new Color(0, 0, 0, 150));
        window.draw(overlay);
    }
}