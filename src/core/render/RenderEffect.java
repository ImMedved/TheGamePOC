package core.render;

public final class RenderEffect {

    public final long id;
    public final int effectTypeId;

    public final float x;
    public final float y;

    public final float progress;

    public RenderEffect(
            long id,
            int effectTypeId,
            float x,
            float y,
            float progress
    ) {
        this.id = id;
        this.effectTypeId = effectTypeId;
        this.x = x;
        this.y = y;
        this.progress = progress;
    }
}