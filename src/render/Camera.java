package render;

import org.jsfml.graphics.Transform;

public final class Camera {

    private float x;
    private float y;

    private float viewportWidth;
    private float viewportHeight;

    public void setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float worldToScreenX(float worldX) {
        return worldX - x + viewportWidth * 0.5f;
    }

    public float worldToScreenY(float worldY) {
        return worldY - y + viewportHeight * 0.5f;
    }

    public Transform buildTransform() {
        float offsetX = -x + viewportWidth * 0.5f;
        float offsetY = -y + viewportHeight * 0.5f;

        Transform transform = new Transform(
                1, 0, offsetX,
                0, 1, offsetY,
                0, 0, 1
        );
        return transform;
    }
}