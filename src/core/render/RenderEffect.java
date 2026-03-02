package core.render;

public final class RenderEffect {

    public final long id;
    public final int effectTypeId;

    public final float x;
    public final float y;

    public final float progress;

    public final float rotation;
    public final float length;

    public RenderEffect(
            long id,
            int effectTypeId,
            float x,
            float y,
            float progress,
            float rotation,
            float length
    ) {
        this.id = id;
        this.effectTypeId = effectTypeId;
        this.x = x;
        this.y = y;
        this.progress = progress;
        this.rotation = rotation;
        this.length = length;
    }
}