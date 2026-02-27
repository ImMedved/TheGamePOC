package render.anim;

import core.render.RenderPlayer;

public final class CharacterAnimationResolver {

    private static final int FRAME_SIZE = 64;
    private static final float ANIMATION_SPEED = 2f;

    private float time;

    public void update(float deltaSeconds) {
        time += deltaSeconds;
    }

    public Frame resolve(RenderPlayer player,
                         float interpX,
                         float interpY) {

        float vx = player.currX - player.prevX;
        float vy = player.currY - player.prevY;

        int direction = resolveDirection(vx, vy);

        boolean moving = Math.abs(vx) > 0.01f || Math.abs(vy) > 0.01f;

        int animColumn = 1;

        if (moving) {
            int phase = (int) (time * ANIMATION_SPEED) % 2;
            animColumn = phase == 0 ? 0 : 2;
        }

        int characterIndex = player.characterId;

        int charRow = characterIndex / 3;
        int charCol = characterIndex % 3;

        int baseX = charCol * (3 * FRAME_SIZE);
        int baseY = charRow * (4 * FRAME_SIZE);

        int u = baseX + animColumn * FRAME_SIZE;
        int v = baseY + direction * FRAME_SIZE;

        return new Frame(u, v, FRAME_SIZE, FRAME_SIZE);
    }

    private int resolveDirection(float vx, float vy) {

        if (Math.abs(vx) > Math.abs(vy)) {
            return vx > 0 ? 2 : 1;
        } else {
            return vy > 0 ? 0 : 3;
        }
    }

    public record Frame(int u, int v, int w, int h) {}
}