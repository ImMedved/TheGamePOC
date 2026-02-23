package input;

public final class InputSnapshot {

    public final int tick;

    public final float moveX;
    public final float moveY;

    public final boolean shoot;

    public final float mouseX;
    public final float mouseY;

    public InputSnapshot(
            int tick,
            float moveX,
            float moveY,
            boolean shoot,
            float mouseX,
            float mouseY
    ) {
        this.tick = tick;
        this.moveX = moveX;
        this.moveY = moveY;
        this.shoot = shoot;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
    @Override
    public String toString() {
        return "Tick=" + tick +
                " move=(" + moveX + "," + moveY + ")" +
                " shoot=" + shoot +
                " mouse=(" + mouseX + "," + mouseY + ")";
    }
}
