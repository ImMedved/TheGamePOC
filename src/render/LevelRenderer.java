package render;

import core.render.LevelRenderData;
import render.batch.VertexBatch;

import org.jsfml.graphics.Color;

public final class LevelRenderer {

    private static final float TILE_SIZE = 32f;

    public void init() {
    }

    public void render(LevelRenderData level,
                       Camera camera,
                       VertexBatch batch) {

        int width = level.width;
        int height = level.height;

        float camX = camera.getX();
        float camY = camera.getY();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int tile = level.textureMap[x][y];
                if (tile == 0)
                    continue;

                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                float screenX = worldX - camX;
                float screenY = worldY - camY;

                batch.addQuad(
                        screenX,
                        screenY,
                        TILE_SIZE,
                        TILE_SIZE,
                        0f, 0f, 1f, 1f,
                        Color.WHITE
                );
            }
        }
    }

    public void shutdown() {
    }
}