package core;

import java.util.List;

public final class WorldState {

    public final int tick;

    public final PlayerState localPlayer;
    public final PlayerState remotePlayer;

    public final List<ProjectileState> projectiles;

    public final boolean gameOver;
    public final int winnerId;

    public WorldState(
            int tick,
            PlayerState localPlayer,
            PlayerState remotePlayer,
            List<ProjectileState> projectiles,
            boolean gameOver,
            int winnerId
    ) {
        this.tick = tick;
        this.localPlayer = localPlayer;
        this.remotePlayer = remotePlayer;
        this.projectiles = List.copyOf(projectiles);
        this.gameOver = gameOver;
        this.winnerId = winnerId;
    }
    public static WorldState initial() {
        return new WorldState(
                0,
                new PlayerState(1, 100, 100, 0, 0, 20),
                new PlayerState(2, 500, 300, 0, 0, 20),
                List.of(),
                false,
                -1
        );
    }
}
