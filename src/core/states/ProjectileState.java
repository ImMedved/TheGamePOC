package core.states;

public final class ProjectileState {

    public final int projectileId;
    public final int ownerId;
    public final float x;
    public final float y;
    public final float dirX;
    public final float dirY;
    public final float speed;
    public final float startX;
    public final float startY;

    public ProjectileState(
            int projectileId,
            int ownerId,
            float x,
            float y,
            float dirX,
            float dirY,
            float speed,
            float startX,
            float startY
    ) {
        this.projectileId = projectileId;
        this.ownerId = ownerId;
        this.x = x;
        this.y = y;
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.startX = startX;
        this.startY = startY;
    }
}
