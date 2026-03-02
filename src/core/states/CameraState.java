package core.states;

public final class CameraState {

    public float x;
    public float y;

    public float viewportWidth;
    public float viewportHeight;

    public CameraState copy() {
        CameraState c = new CameraState();
        c.x = x;
        c.y = y;
        c.viewportWidth = viewportWidth;
        c.viewportHeight = viewportHeight;
        return c;
    }
}