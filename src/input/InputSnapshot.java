package input;

public final class InputSnapshot {

    public final int tick;
    public final long ownerId;

    public final float moveX;
    public final float moveY;

    public final boolean shoot;

    public final float mouseX;
    public final float mouseY;

    public final boolean key1Pressed;
    public final boolean key2Pressed;
    public final boolean key3Pressed;

    public InputSnapshot(
            int tick,
            long ownerId,
            float moveX,
            float moveY,
            boolean shoot,
            float mouseX,
            float mouseY,
            boolean key1Pressed,
            boolean key2Pressed,
            boolean key3Pressed
    ) {
        this.tick = tick;
        this.ownerId = ownerId;

        this.moveX = moveX;
        this.moveY = moveY;

        this.shoot = shoot;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        this.key1Pressed = key1Pressed;
        this.key2Pressed = key2Pressed;
        this.key3Pressed = key3Pressed;
    }

    public InputSnapshot(
            int tick,
            float moveX,
            float moveY,
            boolean shoot,
            float mouseX,
            float mouseY,
            boolean key1Pressed,
            boolean key2Pressed,
            boolean key3Pressed
    ) {
        this(
                tick,
                1,
                moveX,
                moveY,
                shoot,
                mouseX,
                mouseY,
                key1Pressed,
                key2Pressed,
                key3Pressed
        );
    }

    @Override
    public String toString() {
        return "Tick=" + tick +
                " owner=" + ownerId +
                " move=(" + moveX + "," + moveY + ")" +
                " shoot=" + shoot +
                " mouse=(" + mouseX + "," + mouseY + ")";
    }
}