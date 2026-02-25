package core.render;

public final class LevelRenderData {

    public final int width;
    public final int height;

    public final int[][] textureMap;

    public LevelRenderData(
            int width,
            int height,
            int[][] textureMap
    ) {
        this.width = width;
        this.height = height;
        this.textureMap = textureMap;
    }
}