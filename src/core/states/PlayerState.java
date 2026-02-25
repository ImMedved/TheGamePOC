package core.states;

public final class PlayerState {

    public final int playerId;
    public final float x;
    public final float y;
    public final float velocityX;
    public final float velocityY;
    public final float hitboxRadius;

    public PlayerState(
            int playerId,
            float x,
            float y,
            float velocityX,
            float velocityY,
            float hitboxRadius
    ) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.hitboxRadius = hitboxRadius;
    }
}
