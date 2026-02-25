package core.states;

import core.states.effects.EffectData;

import java.util.ArrayList;
import java.util.List;

public final class WorldState implements WorldStateProvider{

    private final PlayerState player;
    private final List<ProjectileState> projectiles;
    private final List<EffectData> effects;
    private final LevelState level;

    public WorldState(PlayerState player, LevelState level) {
        this.player = player;
        this.level = level;
        this.projectiles = new ArrayList<>();
        this.effects = new ArrayList<>();
    }

    public PlayerState player() {
        return player;
    }

    public List<ProjectileState> projectiles() {
        return projectiles;
    }

    public List<EffectData> effects() {
        return effects;
    }

    public LevelState level() {
        return level;
    }

    @Override
    public WorldState getLatestWorldState() {
        return world;
    }
}