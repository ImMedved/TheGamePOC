package core.render;

public final class RenderPlayer {

    public final long id;
    public final int characterId;

    public final float prevX;
    public final float prevY;

    public final float currX;
    public final float currY;

    public final float rotation;

    public RenderPlayer(
            long id,
            int characterId,
            float prevX,
            float prevY,
            float currX,
            float currY,
            float rotation
    ) {
        this.id = id;
        this.characterId = characterId;
        this.prevX = prevX;
        this.prevY = prevY;
        this.currX = currX;
        this.currY = currY;
        this.rotation = rotation;
    }
}