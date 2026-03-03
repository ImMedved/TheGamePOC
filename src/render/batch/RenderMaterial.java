package render.batch;

import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.Texture;

public final class RenderMaterial {

    private final Texture texture;
    private final RenderStates states;

    public RenderMaterial(Texture texture) {
        this.texture = texture;
        this.states = new RenderStates(texture);
    }

    public Texture getTexture() {
        return texture;
    }

    public RenderStates getStates() {
        return states;
    }
}
