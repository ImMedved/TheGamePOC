package core.level;

public final class TileCollisionResolver {

    private TileCollisionResolver() {}

    public static int resolveMask(int textureId) {

        return switch (textureId) {

            // floor1, ladder, floor2, floor3
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