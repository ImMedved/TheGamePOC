package render;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

public final class MenuRenderer {

    private final Runnable onStart;

    private final Texture backgroundTexture;
    private final Texture buttonTexture;

    private Sprite backgroundSprite;
    private Sprite buttonSprite;

    private boolean hover = false;

    public MenuRenderer(ResourceManager resources, Runnable onStart) {
        this.onStart = onStart;

        this.backgroundTexture =
                resources.getTexture(AssetKeys.BACKGROUND);

        this.buttonTexture =
                resources.getTexture(AssetKeys.START_BUTTON);
    }

    public void init(RenderWindow window) {

        backgroundSprite = new Sprite(backgroundTexture);

        float scaleX = 1920f / backgroundTexture.getSize().x;
        float scaleY = 1080f / backgroundTexture.getSize().y;
        backgroundSprite.setScale(scaleX, scaleY);

        buttonSprite = new Sprite(buttonTexture);

        FloatRect bounds = buttonSprite.getGlobalBounds();

        buttonSprite.setPosition(
                960f - bounds.width * 0.5f,
                540f - bounds.height * 0.5f
        );
    }

    public void handleEvent(Event event) {

        if (event.type == Event.Type.MOUSE_MOVED) {

            float mx = event.asMouseEvent().position.x;
            float my = event.asMouseEvent().position.y;

            hover = buttonSprite.getGlobalBounds().contains(mx, my);
        }

        if (event.type == Event.Type.MOUSE_BUTTON_PRESSED) {

            float mx = event.asMouseButtonEvent().position.x;
            float my = event.asMouseButtonEvent().position.y;

            if (buttonSprite.getGlobalBounds().contains(mx, my)) {
                onStart.run();
            }
        }
    }

    public void render(RenderWindow window) {

        window.clear(Color.BLACK);

        window.draw(backgroundSprite);

        if (hover) {
            buttonSprite.setColor(new Color(255, 255, 255, 220));
        } else {
            buttonSprite.setColor(Color.WHITE);
        }

        window.draw(buttonSprite);

        window.display();
    }

    public void shutdown() {
    }
}