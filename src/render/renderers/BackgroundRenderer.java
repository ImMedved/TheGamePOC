package render.renderers;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import render.Camera;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

public final class BackgroundRenderer {

    private final Texture texture;
    private final VertexArray quad;

    private static final float TILE_SIZE = 2676f;

    public BackgroundRenderer(ResourceManager resources) {

        texture = resources.getTexture(AssetKeys.BACKGROUND);
        texture.setRepeated(true);

        quad = new VertexArray(PrimitiveType.QUADS);

        quad.add(new Vertex(new Vector2f(0, 0), Color.WHITE, new Vector2f(0, 0)));
        quad.add(new Vertex(new Vector2f(TILE_SIZE, 0), Color.WHITE, new Vector2f(TILE_SIZE, 0)));
        quad.add(new Vertex(new Vector2f(TILE_SIZE, TILE_SIZE), Color.WHITE, new Vector2f(TILE_SIZE, TILE_SIZE)));
        quad.add(new Vertex(new Vector2f(0, TILE_SIZE), Color.WHITE, new Vector2f(0, TILE_SIZE)));
    }

    public void render(Camera camera, RenderWindow window) {

        float offsetX = -camera.getX() * 0.3f + 960f;
        float offsetY = -camera.getY() * 0.3f + 540f;

        Transform transform = new Transform(
                1, 0, offsetX,
                0, 1, offsetY,
                0, 0, 1
        );

        RenderStates states = new RenderStates(
                BlendMode.ALPHA,
                transform,
                texture,
                null
        );

        window.draw(quad, states);
    }
}