package core.states;

import core.level.TileCollisionFlags;

import java.util.HashMap;
import java.util.Map;

public final class LevelState {

    public int width;
    public int height;

    public int[][] collisionMask;
    public int[][] textureMap;

    public Map<String, Object> attributes;

    public LevelState(int width, int height) {
        this.width = width;
        this.height = height;
        this.collisionMask = new int[width][height];
        this.textureMap = new int[width][height];
        this.attributes = new HashMap<>();
    }

    public LevelState copy() {
        LevelState copy = new LevelState(width, height);

        for (int x = 0; x < width; x++) {
            System.arraycopy(this.collisionMask[x], 0, copy.collisionMask[x], 0, height);
            System.arraycopy(this.textureMap[x], 0, copy.textureMap[x], 0, height);
        }

        copy.attributes = new HashMap<>(this.attributes);

        return copy;
    }

    public int getTileX(float worldX) {
        return (int)(worldX / 100f);
    }

    public int getTileY(float worldY) {
        return (int)(worldY / 100f);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public int getMaskAtWorld(float worldX, float worldY) {
        int tx = getTileX(worldX);
        int ty = getTileY(worldY);
        if (!inBounds(tx, ty)) return TileCollisionFlags.BLOCK_PLAYER | TileCollisionFlags.BLOCK_PROJECTILE;
        return collisionMask[tx][ty];
    }
}