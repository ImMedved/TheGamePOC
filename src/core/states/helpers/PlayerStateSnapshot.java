package core.states.helpers;

public final class PlayerStateSnapshot {

    public final int playerId;
    public final float x;
    public final float y;
    public final float velocityX;
    public final float velocityY;

    public PlayerStateSnapshot(
            int playerId,
            float x,
            float y,
            float velocityX,
            float velocityY
    ) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
}
