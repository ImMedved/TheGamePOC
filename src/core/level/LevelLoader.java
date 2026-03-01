package core.level;

import core.states.LevelState;

public final class LevelLoader {

    private LevelLoader() {}

    public static LevelState createManualLevel() {

        int width = 20;
        int height = 20;

        LevelState level = new LevelState(width, height);

        // ---- manual tile map ----

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                level.textureMap[x][y] = 0; // floor1 by default
            }
        }

        // Example: create a wall rectangle

        for (int x = 5; x < 15; x++) {
            level.textureMap[x][8] = 4; // terminal1
        }

        for (int y = 3; y < 10; y++) {
            level.textureMap[10][y] = 6; // ventilation
        }

        buildCollisionFromTextures(level);

        return level;
    }

    private static void buildCollisionFromTextures(LevelState level) {

        for (int x = 0; x < level.width; x++) {
            for (int y = 0; y < level.height; y++) {

                int textureId = level.textureMap[x][y];

                level.collisionMask[x][y] = switch (textureId) {

                    // floors and ladder
                    case 0, 1, 2, 3 -> 0;

                    // terminal1, terminal2
                    case 4, 5 ->
                            TileCollisionFlags.BLOCK_PLAYER |
                                    TileCollisionFlags.BLOCK_PROJECTILE;

                    // ventilation
                    case 6 ->
                            TileCollisionFlags.BLOCK_PLAYER;

                    default -> 0;
                };
            }
        }
    }
}