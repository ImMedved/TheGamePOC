package core.states.helpers;

public final class Vector2 {

    public float x;
    public float y;

    public Vector2() {
        this(0f, 0f);
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }
}
