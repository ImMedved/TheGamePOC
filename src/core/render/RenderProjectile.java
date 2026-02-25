package core.render;

public final class RenderProjectile {

    public final long id;
    public final int projectileTypeId;

    public final float prevX;
    public final float prevY;

    public final float currX;
    public final float currY;

    public RenderProjectile(
            long id,
            int projectileTypeId,
            float prevX,
            float prevY,
            float currX,
            float currY
    ) {
        this.id = id;
        this.projectileTypeId = projectileTypeId;
        this.prevX = prevX;
        this.prevY = prevY;
        this.currX = currX;
        this.currY = currY;
    }
}