package core.states;

import java.util.HashMap;
import java.util.Map;

public final class LevelState {

    public int width;
    public int height;

    public boolean[][] collisionMap;
    public int[][] textureMap;

    public Map<String, Object> attributes;

    public LevelState(int width, int height) {
        this.width = width;
        this.height = height;
        this.collisionMap = new boolean[width][height];
        this.textureMap = new int[width][height];
        this.attributes = new HashMap<>();
    }

    public LevelState copy() {
        LevelState copy = new LevelState(width, height);

        for (int x = 0; x < width; x++) {
            System.arraycopy(this.collisionMap[x], 0, copy.collisionMap[x], 0, height);
            System.arraycopy(this.textureMap[x], 0, copy.textureMap[x], 0, height);
        }

        copy.attributes = new HashMap<>(this.attributes);

        return copy;
    }
}