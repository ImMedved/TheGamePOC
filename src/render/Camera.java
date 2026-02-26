package render;

import core.render.LevelRenderData;

public final class Camera {

    private float x;
    private float y;

    private float viewportWidth = 1920f;
    private float viewportHeight = 1080f;

    private float deadZonePercent = 0.2f;

    private float minSpeed = 200f;
    private float maxSpeed = 2000f;

    public void setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void update(float targetX,
                       float targetY,
                       LevelRenderData level) {

        float deadZoneWidth = viewportWidth * deadZonePercent;
        float deadZoneHeight = viewportHeight * deadZonePercent;

        float leftBound = x - deadZoneWidth * 0.5f;
        float rightBound = x + deadZoneWidth * 0.5f;

        float topBound = y - deadZoneHeight * 0.5f;
        float bottomBound = y + deadZoneHeight * 0.5f;

        float offsetX = 0f;
        float offsetY = 0f;

        if (targetX < leftBound)
            offsetX = targetX - leftBound;
        else if (targetX > rightBound)
            offsetX = targetX - rightBound;

        if (targetY < topBound)
            offsetY = targetY - topBound;
        else if (targetY > bottomBound)
            offsetY = targetY - bottomBound;

        float distanceX = Math.abs(offsetX);
        float distanceY = Math.abs(offsetY);

        float speedX = computeSpeed(distanceX);
        float speedY = computeSpeed(distanceY);

        x += Math.signum(offsetX) * speedX;
        y += Math.signum(offsetY) * speedY;

        // System.out.println("new frame camera: " + x + "<- x" + y + "<- y");

        clampToLevel(level);
    }

    private float computeSpeed(float distance) {

        if (distance <= 0f)
            return 0f;

        float normalized =
                distance / (viewportWidth * 0.5f);

        float exp =
                (float) (1f - Math.exp(-4f * normalized));

        float speed =
                minSpeed + (maxSpeed - minSpeed) * exp;

        return speed * (1f / 120f);
    }

    private void clampToLevel(LevelRenderData level) {

        float halfW = viewportWidth * 0.5f;
        float halfH = viewportHeight * 0.5f;

        x = Math.max(halfW,
                Math.min((level.width * 100) - halfW, x));

        y = Math.max(halfH,
                Math.min((level.height * 100) - halfH, y));
    }

    public float worldToScreenX(float worldX) {
        return worldX - x + viewportWidth * 0.5f;
    }

    public float worldToScreenY(float worldY) {
        return worldY - y + viewportHeight * 0.5f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}