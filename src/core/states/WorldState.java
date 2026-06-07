package core.states;

import java.util.*;

public final class WorldState {

    public long tickIndex;

    public LinkedHashMap<Long, PlayerState> players;
    public List<ProjectileState> projectiles;
    public List<EffectData> effects;
    public CameraState camera;
    public boolean gameOver;
    public long winnerPlayerId;

    public LevelState level;

    public WorldState() {
        this.players = new LinkedHashMap<>();
        this.projectiles = new ArrayList<>();
        this.effects = new ArrayList<>();
    }

    public static WorldState initial() {
        WorldState world = new WorldState();
        world.tickIndex = 0;
        world.camera = new CameraState();
        world.camera.viewportWidth = 1280f;
        world.camera.viewportHeight = 720f;
        return world;
    }

    public WorldState copy() {
        WorldState copy = new WorldState();
        copy.tickIndex = this.tickIndex;
        copy.camera = this.camera != null ? this.camera.copy() : null;
        copy.gameOver = this.gameOver;
        copy.winnerPlayerId = this.winnerPlayerId;

        for (Map.Entry<Long, PlayerState> entry : this.players.entrySet()) {
            copy.players.put(entry.getKey(), entry.getValue().copy());
        }

        for (ProjectileState p : this.projectiles) copy.projectiles.add(p.copy());
        for (EffectData e : this.effects) copy.effects.add(e.copy());
        if (this.level != null) copy.level = this.level.copy();

        return copy;
    }
}
