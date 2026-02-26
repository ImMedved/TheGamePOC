package render;

import core.render.RenderProjectile;
import render.batch.VertexBatch;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import org.jsfml.graphics.Texture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Color;

import java.util.List;

public final class ProjectileRenderer {

    private static final float WIDTH = 13f;
    private static final float HEIGHT = 40f;

    private final Texture texture;
    private final RenderStates states;

    public ProjectileRenderer(ResourceManager resources) {
        this.texture =
                resources.getTexture(AssetKeys.BULLET);
        this.states = new RenderStates(texture);
    }

    public void init() {
    }

    public RenderStates getStates() {
        return states;
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

            float vx = p.currX - p.prevX;
            float vy = p.currY - p.prevY;

            float angle = 0f;

            if (Math.abs(vx) > 0.0001f || Math.abs(vy) > 0.0001f) {
                angle = (float) Math.toDegrees(Math.atan2(vy, vx)) + 90f;
            }

            float screenX = x - camX;
            float screenY = y - camY;

            addRotatedQuad(
                    batch,
                    screenX,
                    screenY,
                    WIDTH,
                    HEIGHT,
                    angle
            );
        }
    }

    private void addRotatedQuad(VertexBatch batch,
                                float cx,
                                float cy,
                                float w,
                                float h,
                                float angleDeg) {

        float rad = (float) Math.toRadians(angleDeg);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float hw = w * 0.5f;
        float hh = h * 0.5f;

        float[][] pts = {
                {-hw, -hh},
                { hw, -hh},
                { hw,  hh},
                {-hw,  hh}
        };

        var va = batch.getVertexArray();

        for (int i = 0; i < 4; i++) {

            float rx = pts[i][0] * cos - pts[i][1] * sin;
            float ry = pts[i][0] * sin + pts[i][1] * cos;

            float finalX = cx + rx;
            float finalY = cy + ry;

            float u = (i == 0 || i == 3) ? 0f : w;
            float v = (i < 2) ? 0f : h;

            va.add(new org.jsfml.graphics.Vertex(
                    new org.jsfml.system.Vector2f(finalX, finalY),
                    org.jsfml.graphics.Color.WHITE,
                    new org.jsfml.system.Vector2f(u, v)
            ));
        }
    }

    public void shutdown() {
    }
}