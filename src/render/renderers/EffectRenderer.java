package render.renderers;

import core.render.RenderEffect;
import org.jsfml.graphics.Vertex;
import org.jsfml.system.Vector2f;
import render.Camera;
import render.batch.BatchManager;
import render.batch.RenderMaterial;
import render.batch.VertexBatch;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import org.jsfml.graphics.Texture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Color;

import java.util.List;

public final class EffectRenderer {

    private final Texture dashTexture;
    private final Texture speedTexture;
    private final Texture bulletHoleTexture;

    private final RenderStates dashStates;
    private final RenderStates speedStates;
    private final RenderStates holeStates;

    private final RenderMaterial dashMaterial;
    private final RenderMaterial speedMaterial;
    private final RenderMaterial holeMaterial;

    public EffectRenderer(ResourceManager resources) {

        this.dashTexture = resources.getTexture(AssetKeys.DASH_TRACE);
        this.speedTexture = resources.getTexture(AssetKeys.SPEED_BOOST);
        this.bulletHoleTexture = resources.getTexture(AssetKeys.BULLET_HOLE);

        this.dashStates = new RenderStates(dashTexture);
        this.speedStates = new RenderStates(speedTexture);
        this.holeStates = new RenderStates(bulletHoleTexture);

        this.dashMaterial = new RenderMaterial(dashTexture);
        this.speedMaterial = new RenderMaterial(speedTexture);
        this.holeMaterial = new RenderMaterial(bulletHoleTexture);
    }

    public RenderStates getDashStates() {
        return dashStates;
    }

    public RenderStates getSpeedStates() {
        return speedStates;
    }

    public RenderStates getHoleStates() {
        return holeStates;
    }

    public void render(List<RenderEffect> effects,
                       Camera camera,
                       BatchManager batchManager) {

        for (RenderEffect e : effects) {
            //System.out.println("effects count: " + effects.size());

            float screenX = camera.worldToScreenX(e.x);
            float screenY = camera.worldToScreenY(e.y);

            switch (e.type) {

                case DASH_TRACE -> {
                    VertexBatch batch = batchManager.getBatch(dashMaterial);
                    renderDash(e, camera, batch);
                    //System.out.println("Trigger DASH_TRACE");

                }

                case SPEED_AURA -> {
                    VertexBatch batch = batchManager.getBatch(speedMaterial);
                    renderSpeed(e, screenX, screenY, batch);
                }

                case BULLET_HOLE -> {
                    VertexBatch batch = batchManager.getBatch(holeMaterial);
                    renderBulletHole(screenX, screenY, batch);
                }
            }
        }
    }

    private void renderDash(RenderEffect e,
                            Camera camera,
                            VertexBatch batch) {

        float startX = e.x;
        float startY = e.y;

        float endX = startX + e.dx;
        float endY = startY + e.dy;

        float dx = endX - startX;
        float dy = endY - startY;

        float length = (float)Math.sqrt(dx * dx + dy * dy);
        System.out.println("dx: " + e.dx + " dy: " + e.dy);
        if (length <= 0.001f) return;

        float nx = dx / length;
        float ny = dy / length;

        float px = -ny;
        float py = nx;

        float halfWidth = 16f;
        float halfLen = length * 0.5f;

        float centerX = (startX + endX) * 0.5f;
        float centerY = (startY + endY) * 0.5f;

        float x0 = centerX - nx * halfLen - px * halfWidth;
        float y0 = centerY - ny * halfLen - py * halfWidth;

        float x1 = centerX + nx * halfLen - px * halfWidth;
        float y1 = centerY + ny * halfLen - py * halfWidth;

        float x2 = centerX + nx * halfLen + px * halfWidth;
        float y2 = centerY + ny * halfLen + py * halfWidth;

        float x3 = centerX - nx * halfLen + px * halfWidth;
        float y3 = centerY - ny * halfLen + py * halfWidth;

        float sx0 = camera.worldToScreenX(x0);
        float sy0 = camera.worldToScreenY(y0);

        float sx1 = camera.worldToScreenX(x1);
        float sy1 = camera.worldToScreenY(y1);

        float sx2 = camera.worldToScreenX(x2);
        float sy2 = camera.worldToScreenY(y2);

        float sx3 = camera.worldToScreenX(x3);
        float sy3 = camera.worldToScreenY(y3);

        float texW = dashTexture.getSize().x;
        float texH = dashTexture.getSize().y;

        int alpha = (int)(255 * (1f - e.progress));

        var color = new Color(255, 255, 255, alpha);

        batch.getVertexArray().add(new Vertex(new Vector2f(sx0, sy0), color, new Vector2f(0, texH)));
        batch.getVertexArray().add(new Vertex(new Vector2f(sx1, sy1), color, new Vector2f(0, 0)));
        batch.getVertexArray().add(new Vertex(new Vector2f(sx2, sy2), color, new Vector2f(texW, 0)));
        batch.getVertexArray().add(new Vertex(new Vector2f(sx3, sy3), color, new Vector2f(texW, texH)));
    }

    private void renderSpeed(RenderEffect e,
                             float centerX,
                             float centerY,
                             VertexBatch batch) {

        float size = 64f;
        float offsetY = -100f;

        batch.addQuad(
                centerX - size * 0.5f,
                centerY + offsetY,
                size,
                size,
                0f,
                0f,
                speedTexture.getSize().x,
                speedTexture.getSize().y,
                Color.WHITE
        );
    }

    private void renderBulletHole(float x, float y, VertexBatch batch) {

        float size = 99f;

        batch.addQuad(
                x - size * 0.5f,
                y - size * 0.5f,
                size,
                size,
                0f,
                0f,
                99f,
                99f,
                Color.WHITE
        );
    }
    public void init() {
    }
}