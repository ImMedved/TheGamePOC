package render;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

public class AnimatedCharacter {

    private static final int FRAME_SIZE = 64;
    private static final int FRAMES_PER_ROW = 3;

    private static final float ANIMATION_SPEED = 3f; // 3 кадра в секунду

    private final Texture atlas;
    private final Sprite sprite;

    private final int characterIndex; // 0..5

    private float animationTimer = 0f;
    private int frameIndex = 1; // начинаем со "стоит"
    private int frameDirection = 1;

    private Direction direction = Direction.DOWN;

    public AnimatedCharacter(Texture atlas, int characterIndex) {
        this.atlas = atlas;
        this.characterIndex = characterIndex;
        this.sprite = new Sprite(atlas);
    }

    public void update(float dt, float velocityX, float velocityY) {

        updateDirection(velocityX, velocityY);

        if (velocityX != 0 || velocityY != 0) {

            animationTimer += dt;

            if (animationTimer >= 1f / ANIMATION_SPEED) {

                animationTimer = 0f;

                frameIndex += frameDirection;

                if (frameIndex == 2 || frameIndex == 0) {
                    frameDirection *= -1;
                }
            }

        } else {
            frameIndex = 1; // стоим
        }

        updateTextureRect();
    }

    private void updateDirection(float vx, float vy) {

        if (Math.abs(vx) > Math.abs(vy)) {
            direction = vx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (vy != 0) {
            direction = vy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    private void updateTextureRect() {

        int characterCol = characterIndex % 3;
        int characterRow = characterIndex / 3;

        int baseX = characterCol * FRAMES_PER_ROW * FRAME_SIZE;
        int baseY = characterRow * 4 * FRAME_SIZE;

        int texX = baseX + frameIndex * FRAME_SIZE;
        int texY = baseY + direction.row * FRAME_SIZE;

        sprite.setTextureRect(new IntRect(
                texX,
                texY,
                FRAME_SIZE,
                FRAME_SIZE
        ));
    }

    public void draw(RenderWindow window, float x, float y) {

        sprite.setOrigin(FRAME_SIZE / 2f, FRAME_SIZE / 2f);
        sprite.setPosition(new Vector2f(x, y));

        window.draw(sprite);
    }
}