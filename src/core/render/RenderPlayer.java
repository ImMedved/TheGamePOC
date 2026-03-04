package core.render;

public final class RenderPlayer {

    public final long id;
    public final int characterId;

    public final float prevX;
    public final float prevY;

    public final float currX;
    public final float currY;

    public final float rotation;

    public final float shootCooldown;
    public final float tripleShotCooldown;
    public final float speedCooldown;
    public final float blinkCooldown;

    public final float health;
    public final float maxHealth;

    public RenderPlayer(
            long id,
            int characterId,
            float prevX,
            float prevY,
            float currX,
            float currY,
            float rotation,
            float shootCooldown,
            float tripleShotCooldown,
            float speedCooldown,
            float blinkCooldown,
            float health,
            float maxHealth
    ) {
        this.id = id;
        this.characterId = characterId;
        this.prevX = prevX;
        this.prevY = prevY;
        this.currX = currX;
        this.currY = currY;
        this.rotation = rotation;
        this.shootCooldown = shootCooldown;
        this.tripleShotCooldown = tripleShotCooldown;
        this.speedCooldown = speedCooldown;
        this.blinkCooldown = blinkCooldown;
        this.health = health;
        this.maxHealth = maxHealth;
    }
}