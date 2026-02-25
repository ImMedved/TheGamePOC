package core.states;

public class BulletHoleState {

    public final float x;
    public final float y;
    public float lifetime;

    public BulletHoleState(float x, float y) {
        this.x = x;
        this.y = y;
        this.lifetime = 3f;
    }
}