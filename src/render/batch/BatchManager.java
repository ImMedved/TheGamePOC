package render.batch;

import org.jsfml.graphics.PrimitiveType;

import java.util.HashMap;
import java.util.Map;

public final class BatchManager {

    private final Map<RenderMaterial, VertexBatch> batches = new HashMap<>();

    public void beginFrame() {
        for (VertexBatch batch : batches.values()) {
            batch.clear();
        }
    }

    public VertexBatch getBatch(RenderMaterial material) {
        return batches.computeIfAbsent(
                material,
                m -> new VertexBatch(PrimitiveType.QUADS)
        );
    }

    public Map<RenderMaterial, VertexBatch> getAll() {
        return batches;
    }
}