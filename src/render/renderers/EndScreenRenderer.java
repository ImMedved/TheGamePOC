package render.renderers;

import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Texture;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

public final class EndScreenRenderer {

    private final Texture winTexture;
    private final Texture loseTexture;

    public EndScreenRenderer(ResourceManager resources) {
        this.winTexture = resources.hasTexture(AssetKeys.WIN) ? resources.getTexture(AssetKeys.WIN) : null;
        this.loseTexture = resources.hasTexture(AssetKeys.LOSE) ? resources.getTexture(AssetKeys.LOSE) : null;
    }

    public void render(RenderWindow window, boolean won) {
        window.clear(won ? new Color(20, 70, 35) : new Color(80, 25, 25));

        Texture texture = won ? winTexture : loseTexture;
        if (texture == null) {
            window.display();
            return;
        }

        Sprite sprite = new Sprite(texture);
        float scaleX = (float) window.getSize().x / texture.getSize().x;
        float scaleY = (float) window.getSize().y / texture.getSize().y;
        sprite.setScale(scaleX, scaleY);
        window.draw(sprite);
        window.display();
    }
}
