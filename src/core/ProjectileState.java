package core;

public final class ProjectileState {

    public final int projectileId;
    public final int ownerId;
    public final float x;
    public final float y;
    public final float dirX;
    public final float dirY;
    public final float speed;

    public ProjectileState(
            int projectileId,
            int ownerId,
            float x,
            float y,
            float dirX,
            float dirY,
            float speed
    ) {
        this.projectileId = projectileId;
        this.ownerId = ownerId;
        this.x = x;
        this.y = y;
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
    }
}
