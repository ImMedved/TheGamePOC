package render;

import core.render.LevelRenderData;
import render.batch.VertexBatch;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import org.jsfml.graphics.Texture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Color;

public final class LevelRenderer {

    private static final float TILE_SIZE = 100f;

    private final Texture tilesTexture;
    private final RenderStates states;

    public LevelRenderer(ResourceManager resources) {
        this.tilesTexture =
                resources.getTexture(AssetKeys.TILES);
        this.states = new RenderStates(tilesTexture);
    }

    public void init() {
    }

    public RenderStates getStates() {
        return states;
    }

    public void render(LevelRenderData level,
                       Camera camera,
                       VertexBatch batch) {

        float camX = camera.getX();
        float camY = camera.getY();

        int width = level.width;
        int height = level.height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int tile = level.textureMap[x][y];
                if (tile < 0) continue;

                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                float screenX = worldX - camX;
                float screenY = worldY - camY;

                float u0 = tile * TILE_SIZE;
                float v0 = 0f;
                float u1 = u0 + TILE_SIZE;
                float v1 = TILE_SIZE;

                batch.addQuad(
                        screenX,
                        screenY,
                        TILE_SIZE,
                        TILE_SIZE,
                        u0, v0, u1, v1,
                        Color.WHITE
                );
            }
        }
    }

    public void shutdown() {
    }
}