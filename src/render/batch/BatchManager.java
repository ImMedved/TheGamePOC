package render.batch;

import org.jsfml.graphics.PrimitiveType;

public final class BatchManager {

    private final VertexBatch levelBatch =
            new VertexBatch(PrimitiveType.QUADS);

    private final VertexBatch playerBatch =
            new VertexBatch(PrimitiveType.QUADS);

    private final VertexBatch projectileBatch =
            new VertexBatch(PrimitiveType.QUADS);

    private final VertexBatch effectBatch =
            new VertexBatch(PrimitiveType.QUADS);

    public void beginFrame() {
        levelBatch.clear();
        playerBatch.clear();
        projectileBatch.clear();
        effectBatch.clear();
    }

    public VertexBatch level() {
        return levelBatch;
    }

    public VertexBatch players() {
        return playerBatch;
    }

    public VertexBatch projectiles() {
        return projectileBatch;
    }

    public VertexBatch effects() {
        return effectBatch;
    }
}