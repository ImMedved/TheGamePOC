package render;

import core.PlayerState;
import core.ProjectileState;
import core.WorldState;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

public class EntityRenderer {

    private final CircleShape localPlayerShape;
    private final CircleShape remotePlayerShape;
    private final CircleShape projectileShape;

    public EntityRenderer() {

        localPlayerShape = new CircleShape();
        localPlayerShape.setFillColor(Color.GREEN);

        remotePlayerShape = new CircleShape();
        remotePlayerShape.setFillColor(Color.RED);

        projectileShape = new CircleShape(4f);
        projectileShape.setFillColor(Color.WHITE);
    }

    public void drawEntities(RenderWindow window, WorldState state) {

        drawPlayer(window, state.localPlayer, localPlayerShape);
        drawPlayer(window, state.remotePlayer, remotePlayerShape);
        drawProjectiles(window, state.projectiles);
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