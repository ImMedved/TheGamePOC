package render;

import core.render.RenderProjectile;
import render.batch.VertexBatch;

import org.jsfml.graphics.Color;

import java.util.List;

public final class ProjectileRenderer {

    private static final float SIZE = 12f;

    public void init() {
    }

    public void render(List<RenderProjectile> projectiles,
                       Camera camera,
                       float alpha,
                       VertexBatch batch) {

        float camX = camera.getX();
        float camY = camera.getY();

        for (RenderProjectile p : projectiles) {

            float x = p.prevX + (p.currX - p.prevX) * alpha;
            float y = p.prevY + (p.currY - p.prevY) * alpha;

            float screenX = x - camX;
            float screenY = y - camY;

            batch.addQuad(
                    screenX - SIZE * 0.5f,
                    screenY - SIZE * 0.5f,
                    SIZE,
                    SIZE,
                    0f, 0f, 1f, 1f,
                    Color.RED
            );
        }
    }

    public void shutdown() {
    }
}