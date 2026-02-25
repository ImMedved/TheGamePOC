package render.renderers;

import core.states.PlayerState;
import core.states.WorldState;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import render.Direction;

public class CharacterBatchRenderer {

    private static final int FRAME_SIZE = 64;
    private static final int FRAMES_PER_ROW = 3;
    private static final int DIRECTIONS = 4;

    private static final float ANIMATION_SPEED = 3f;

    private final Texture atlas;
    private final VertexArray vertices;

    private float localAnimTimer = 0f;
    private float remoteAnimTimer = 0f;

    private int localFrame = 1;
    private int remoteFrame = 1;

    private int localFrameDir = 1;
    private int remoteFrameDir = 1;

    private final int localCharacterIndex;
    private final int remoteCharacterIndex;

    public CharacterBatchRenderer() {

        atlas = new Texture();

        try {
            atlas.loadFromFile(java.nio.file.Paths.get("assets/chars.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        vertices = new VertexArray(PrimitiveType.QUADS);

        localCharacterIndex = (int)(Math.random() * 6);
        remoteCharacterIndex = (int)(Math.random() * 6);
    }

    public void draw(RenderWindow window, WorldState state, float dt) {

        vertices.clear();

        updateAnimation(state.localPlayer, true, dt);
        updateAnimation(state.remotePlayer, false, dt);

        buildCharacterQuad(state.localPlayer,
                localCharacterIndex,
                localFrame);

        buildCharacterQuad(state.remotePlayer,
                remoteCharacterIndex,
                remoteFrame);

        RenderStates statesRender = new RenderStates(atlas);
        window.draw(vertices, statesRender);
    }

    private void updateAnimation(PlayerState player,
                                 boolean isLocal,
                                 float dt) {

        float vx = player.velocityX;
        float vy = player.velocityY;

        if (vx == 0 && vy == 0) {
            if (isLocal) localFrame = 1;
            else remoteFrame = 1;
            return;
        }

        if (isLocal) {
            localAnimTimer += dt;
            if (localAnimTimer >= 1f / ANIMATION_SPEED) {
                localAnimTimer = 0f;
                localFrame += localFrameDir;
                if (localFrame == 2 || localFrame == 0)
                    localFrameDir *= -1;
            }
        } else {
            remoteAnimTimer += dt;
            if (remoteAnimTimer >= 1f / ANIMATION_SPEED) {
                remoteAnimTimer = 0f;
                remoteFrame += remoteFrameDir;
                if (remoteFrame == 2 || remoteFrame == 0)
                    remoteFrameDir *= -1;
            }
        }
    }

    private void buildCharacterQuad(PlayerState player,
                                    int characterIndex,
                                    int frameIndex) {

        Direction dir = resolveDirection(
                player.velocityX,
                player.velocityY
        );

        int characterCol = characterIndex % 3;
        int characterRow = characterIndex / 3;

        int baseX = characterCol * FRAMES_PER_ROW * FRAME_SIZE;
        int baseY = characterRow * DIRECTIONS * FRAME_SIZE;

        int texX = baseX + frameIndex * FRAME_SIZE;
        int texY = baseY + dir.row * FRAME_SIZE;

        float x = player.x - FRAME_SIZE / 2f;
        float y = player.y - FRAME_SIZE / 2f;

        vertices.add(new Vertex(
                new Vector2f(x, y),
                new Vector2f(texX, texY)
        ));

        vertices.add(new Vertex(
                new Vector2f(x + FRAME_SIZE, y),
                new Vector2f(texX + FRAME_SIZE, texY)
        ));

        vertices.add(new Vertex(
                new Vector2f(x + FRAME_SIZE, y + FRAME_SIZE),
                new Vector2f(texX + FRAME_SIZE, texY + FRAME_SIZE)
        ));

        vertices.add(new Vertex(
                new Vector2f(x, y + FRAME_SIZE),
                new Vector2f(texX, texY + FRAME_SIZE)
        ));
    }

    private Direction resolveDirection(float vx, float vy) {

        if (Math.abs(vx) > Math.abs(vy)) {
            return vx > 0 ? Direction.RIGHT : Direction.LEFT;
        }

        if (vy != 0) {
            return vy > 0 ? Direction.DOWN : Direction.UP;
        }

        return Direction.DOWN;
    }
}