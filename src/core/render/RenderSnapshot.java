package core.render;

import java.util.List;

public final class RenderSnapshot {

    public final long tickIndex;

    public final List<RenderPlayer> players;
    public final List<RenderProjectile> projectiles;
    public final List<RenderEffect> effects;

    public final float camX;
    public final float camY;

    public final LevelRenderData level;

    public RenderSnapshot(
            long tickIndex,
            List<RenderPlayer> players,
            List<RenderProjectile> projectiles,
            List<RenderEffect> effects,
            float camX,
            float camY,
            LevelRenderData level
    ) {
        this.tickIndex = tickIndex;
        this.players = players;
        this.projectiles = projectiles;
        this.effects = effects;
        this.camX = camX;
        this.camY = camY;
        this.level = level;
    }
}