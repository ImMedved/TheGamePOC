package render;

import core.render.RenderEffect;
import render.batch.VertexBatch;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import org.jsfml.graphics.Texture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Color;

import java.util.List;

public final class EffectRenderer {

    private static final float SIZE = 99f;

    private final Texture texture;
    private final RenderStates states;

    public EffectRenderer(ResourceManager resources) {
        this.texture = resources.getTexture(AssetKeys.BULLET_HOLE);
        this.states = new RenderStates(texture);
    }

    public void init() {
    }

    public RenderStates getStates() {
        return states;
    }

    public void render(List<RenderEffect> effects,
                       Camera camera,
                       VertexBatch batch) {

        float camX = camera.getX();
        float camY = camera.getY();

        for (RenderEffect e : effects) {

            float screenX = e.x - camX + 960f;
            float screenY = e.y - camY + 540f;

            float size = 99f;
            float half = size * 0.5f;

            batch.addQuad(
                    screenX - half,
                    screenY - half,
                    size,
                    size,
                    0f, 0f, 99f, 99f,
                    Color.WHITE
            );
        }
    }

    public void shutdown() {
    }
}