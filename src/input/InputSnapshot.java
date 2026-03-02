package input;

public final class InputSnapshot {

    public final int tick;

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
        this.moveX = moveX;
        this.moveY = moveY;
        this.shoot = shoot;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.key1Pressed = key1Pressed;
        this.key2Pressed = key2Pressed;
        this.key3Pressed = key3Pressed;
    }
    @Override
    public String toString() {
        return "Tick=" + tick +
                " move=(" + moveX + "," + moveY + ")" +
                " shoot=" + shoot +
                " mouse=(" + mouseX + "," + mouseY + ")";
    }
}
