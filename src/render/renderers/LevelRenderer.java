package render.renderers;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.nio.file.Paths;

public class LevelRenderer {

    private static final int TILE_SIZE = 100;
    private static final int TILES_X = 20;
    private static final int TILES_Y = 20;

    private final VertexArray vertices;
    private final Texture atlas;

    public LevelRenderer() {

        atlas = new Texture();

        try {
            atlas.loadFromFile(Paths.get("assets/tiles.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        vertices = new VertexArray(PrimitiveType.QUADS);

        buildMesh();
    }

    private void buildMesh() {

        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {

                boolean isWall =
                        x == 0 || y == 0 ||
                                x == TILES_X - 1 ||
                                y == TILES_Y - 1;

                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                float texOffsetX = isWall ? TILE_SIZE : 0f;

                vertices.add(new Vertex(
                        new Vector2f(worldX, worldY),
                        new Vector2f(texOffsetX, 0)
                ));

                vertices.add(new Vertex(
                        new Vector2f(worldX + TILE_SIZE, worldY),
                        new Vector2f(texOffsetX + TILE_SIZE, 0)
                ));

                vertices.add(new Vertex(
                        new Vector2f(worldX + TILE_SIZE, worldY + TILE_SIZE),
                        new Vector2f(texOffsetX + TILE_SIZE, TILE_SIZE)
                ));

                vertices.add(new Vertex(
                        new Vector2f(worldX, worldY + TILE_SIZE),
                        new Vector2f(texOffsetX, TILE_SIZE)
                ));
            }
        }
    }

    public void draw(RenderWindow window) {
        RenderStates states = new RenderStates(atlas);
        window.draw(vertices, states);
    }
}