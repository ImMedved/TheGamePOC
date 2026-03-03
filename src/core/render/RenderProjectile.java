package core.render;

public final class RenderProjectile {

    public final long id;
    public final int projectileTypeId;

    public final float prevX;
    public final float prevY;

    public final float currX;
    public final float currY;

    public final int ownerCharacterId;

    public RenderProjectile(
            long id,
            int projectileTypeId,
            float prevX,
            float prevY,
            float currX,
            float currY,
            int ownerCharacterId
    ) {
        this.id = id;
        this.projectileTypeId = projectileTypeId;
        this.prevX = prevX;
        this.prevY = prevY;
        this.currX = currX;
        this.currY = currY;
        this.ownerCharacterId = ownerCharacterId;
    }
}