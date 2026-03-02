package core.systems;

import core.SimulationContext;
import core.commands.ChangeCameraCommand;
import core.states.CameraState;
import core.states.LevelState;
import core.states.PlayerState;

public final class CameraSystem implements GameSystem {

    private static final float DEAD_ZONE = 150f;

    @Override
    public Phase phase() {
        return Phase.SEQUENTIAL;
    }

    @Override
    public void update(SimulationContext context) {

        CameraState camera = context.snapshot().camera;
        LevelState level = context.snapshot().level;

        if (camera == null || level == null) return;
        if (context.snapshot().players.isEmpty()) return;

        PlayerState player =
                context.snapshot().players.values().iterator().next();

        float camX = camera.x;
        float camY = camera.y;

        float dx = player.position.x - camX;
        float dy = player.position.y - camY;

        if (Math.abs(dx) > DEAD_ZONE) {
            camX += dx - Math.signum(dx) * DEAD_ZONE;
        }

        if (Math.abs(dy) > DEAD_ZONE) {
            camY += dy - Math.signum(dy) * DEAD_ZONE;
        }

        float halfW = camera.viewportWidth * 0.5f;
        float halfH = camera.viewportHeight * 0.5f;

        float maxX = level.width * 100f - halfW;
        float maxY = level.height * 100f - halfH;

        camX = clamp(camX, halfW, maxX);
        camY = clamp(camY, halfH, maxY);

        context.addCommand(new ChangeCameraCommand(camX, camY));
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}