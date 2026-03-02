package core.level;

import core.states.LevelState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LevelLoader {

    private LevelLoader() {}

    public static LevelState loadFromAscii(Path path) {

        List<String> lines;

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load level file: " + path, e);
        }

        int height = lines.size();
        if (height == 0) {
            throw new IllegalArgumentException("Level file is empty");
        }

        int width = lines.get(0).length();

        LevelState level = new LevelState(width, height);

        for (int y = 0; y < height; y++) {

            String line = lines.get(y);

            if (line.length() != width) {
                throw new IllegalArgumentException("Inconsistent row width at line " + y);
            }

            for (int x = 0; x < width; x++) {

                char c = line.charAt(x);

                if (!Character.isDigit(c)) {
                    throw new IllegalArgumentException("Invalid tile character: " + c);
                }

                int textureId = c - '0';

                level.textureMap[x][y] = textureId;
                level.collisionMask[x][y] = resolveMask(textureId);
            }
        }

        return level;
    }

    private static int resolveMask(int textureId) {

        return switch (textureId) {

            case 0, 1, 2, 3 -> 0;

            case 4, 5, 7, 8 ->
                    TileCollisionFlags.BLOCK_PLAYER |
                            TileCollisionFlags.BLOCK_PROJECTILE;

            case 6 ->
                    TileCollisionFlags.BLOCK_PLAYER;

            default -> 0;
        };
    }
}