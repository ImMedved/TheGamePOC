package render;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.nio.file.Paths;

public class LevelRenderer {

    private static final int TILE_SIZE = 100;
    private static final int TILES_X = 20;
    private static final int TILES_Y = 20;

    private final RectangleShape[][] tiles;

    public LevelRenderer() {

        Texture tileTexture = new Texture();
        Texture wallTexture = new Texture();

        try {
            tileTexture.loadFromFile(Paths.get("assets/tile.png"));
            wallTexture.loadFromFile(Paths.get("assets/wallTile.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tiles = new RectangleShape[TILES_X][TILES_Y];

        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {

                RectangleShape tile =
                        new RectangleShape(
                                new Vector2f(TILE_SIZE, TILE_SIZE)
                        );

                boolean isWall =
                        x == 0 || y == 0 ||
                                x == TILES_X - 1 ||
                                y == TILES_Y - 1;

                tile.setTexture(isWall ? wallTexture : tileTexture);
                tile.setPosition(x * TILE_SIZE, y * TILE_SIZE);

                tiles[x][y] = tile;
            }
        }
    }

    public void draw(RenderWindow window) {
        for (int x = 0; x < TILES_X; x++)
            for (int y = 0; y < TILES_Y; y++)
                window.draw(tiles[x][y]);
    }
}