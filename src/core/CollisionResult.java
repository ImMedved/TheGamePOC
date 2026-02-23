package core;

import java.util.List;

/**
 * Мок, который не работает. Оно сбрасывает состояние игры, а не перезапускает или не дропает главное меню.
 */
public final class CollisionResult {

    public final List<ProjectileState> projectiles;
    public final boolean gameOver;
    public final int winnerId;

    public CollisionResult(
            List<ProjectileState> projectiles,
            boolean gameOver,
            int winnerId
    ) {
        this.projectiles = projectiles;
        this.gameOver = gameOver;
        this.winnerId = winnerId;
    }
}
