package core.render;

import core.states.EffectType;

public final class RenderEffect {

    public final long id;
    public final EffectType type;

    public final float x;
    public final float y;

    public final float progress;

    public final float dx;
    public final float dy;

    public final float scaleX;
    public final float scaleY;

    public final int characterId;

    public RenderEffect(
            long id,
            EffectType type,
            float x,
            float y,
            float progress,
            float dx,
            float dy,
            float scaleX,
            float scaleY,
            int characterId
    ) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.progress = progress;
        this.dx = dx;
        this.dy = dy;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.characterId = characterId;
    }
}