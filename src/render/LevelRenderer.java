package render;

import core.render.LevelRenderData;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

public final class LevelRenderer {

    private static final float TILE_SIZE = 100f;

    private final Texture tilesTexture;

    private VertexArray geometry;

    public LevelRenderer(ResourceManager resources) {
        this.tilesTexture = resources.getTexture(AssetKeys.TILES);
    }

    public void init(LevelRenderData level) {

        geometry = new VertexArray(PrimitiveType.QUADS);

        int width = level.width;
        int height = level.height;
        int[][] textureMap = level.textureMap;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int tile = textureMap[x][y];
                if (tile < 0) continue;

                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                float u0 = tile * TILE_SIZE;
                float v0 = 0f;
                float u1 = u0 + TILE_SIZE;
                float v1 = TILE_SIZE;

                geometry.add(new Vertex(
                        new Vector2f(worldX, worldY),
                        Color.WHITE,
                        new Vector2f(u0, v0)
                ));

                geometry.add(new Vertex(
                        new Vector2f(worldX + TILE_SIZE, worldY),
                        Color.WHITE,
                        new Vector2f(u1, v0)
                ));

                geometry.add(new Vertex(
                        new Vector2f(worldX + TILE_SIZE, worldY + TILE_SIZE),
                        Color.WHITE,
                        new Vector2f(u1, v1)
                ));

                geometry.add(new Vertex(
                        new Vector2f(worldX, worldY + TILE_SIZE),
                        Color.WHITE,
                        new Vector2f(u0, v1)
                ));
            }
        }
    }

    public void render(Camera camera, RenderWindow window) {

        if (geometry == null) return;

        float offsetX = -camera.getX() + 960f;
        float offsetY = -camera.getY() + 540f;

        Transform transform = new Transform(
                1, 0, offsetX,
                0, 1, offsetY,
                0, 0, 1
        );

        RenderStates states = new RenderStates(
                BlendMode.ALPHA,
                transform,
                tilesTexture,
                null
        );

        window.draw(geometry, states);
    }

    public void shutdown() {
    }
}