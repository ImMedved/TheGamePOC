package render;

import core.render.RenderPlayer;
import render.batch.VertexBatch;
import render.resources.AssetKeys;
import render.resources.ResourceManager;
import render.anim.CharacterAnimationResolver;

import org.jsfml.graphics.Texture;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Color;

import java.util.List;

public final class PlayerRenderer {

    private static final float SIZE = 64f;

    private final Texture texture;
    private final RenderStates states;

    private final CharacterAnimationResolver animation =
            new CharacterAnimationResolver();

    public PlayerRenderer(ResourceManager resources) {
        this.texture =
                resources.getTexture(AssetKeys.CHARS);
        this.states = new RenderStates(texture);
    }

    public void init() {
    }

    public RenderStates getStates() {
        return states;
    }

    public void render(List<RenderPlayer> players,
                       Camera camera,
                       float alpha,
                       VertexBatch batch) {

        float camX = camera.getX();
        float camY = camera.getY();

        animation.update(1f / 120f);

        for (RenderPlayer p : players) {

            float x = p.prevX + (p.currX - p.prevX) * alpha;
            float y = p.prevY + (p.currY - p.prevY) * alpha;
            // System.out.println("new frame player: " + x + "<- x" + y + "<- y");

            float screenX = camera.worldToScreenX(x);
            float screenY = camera.worldToScreenY(y);

            var frame = animation.resolve(p, x, y);

            batch.addQuad(
                    screenX - SIZE * 0.5f,
                    screenY - SIZE * 0.5f,
                    SIZE,
                    SIZE,
                    frame.u(),
                    frame.v(),
                    frame.u() + frame.w(),
                    frame.v() + frame.h(),
                    Color.WHITE
            );
        }
    }

    public void shutdown() {
    }
}