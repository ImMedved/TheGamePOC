package render;

import core.render.RenderEffect;
import render.batch.VertexBatch;

import org.jsfml.graphics.Color;

import java.util.List;

public final class EffectRenderer {

    private static final float SIZE = 30f;

    public void init() {
    }

    public void render(List<RenderEffect> effects,
                       Camera camera,
                       VertexBatch batch) {

        float camX = camera.getX();
        float camY = camera.getY();

        for (RenderEffect e : effects) {

            float screenX = e.x - camX;
            float screenY = e.y - camY;

            float scale = 1f - e.progress;

            batch.addQuad(
                    screenX - SIZE * 0.5f * scale,
                    screenY - SIZE * 0.5f * scale,
                    SIZE * scale,
                    SIZE * scale,
                    0f, 0f, 1f, 1f,
                    Color.YELLOW
            );
        }
    }

    public void shutdown() {
    }
}