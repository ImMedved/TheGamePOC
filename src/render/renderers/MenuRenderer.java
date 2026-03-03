package render.renderers;

import org.jsfml.graphics.*;
import org.jsfml.window.event.Event;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

public final class MenuRenderer {

    private final Runnable onStart;

    private final Texture backgroundTexture;
    private final Texture startButtonTexture;
    private final Texture charactersTexture;

    private Sprite backgroundSprite;
    private Sprite startButtonSprite;

    private Sprite[] characterButtons;

    private boolean startHover = false;
    private int selectedCharacterId = 0;

    private static final int CHARACTER_COUNT = 3;

    private static final int FRAME_SIZE = 64;
    private static final int CHAR_ROWS = 4;
    private static final int CHAR_COLS = 3;

    public MenuRenderer(ResourceManager resources, Runnable onStart) {
        this.onStart = onStart;
        this.backgroundTexture = resources.getTexture(AssetKeys.BACKGROUND);
        this.startButtonTexture = resources.getTexture(AssetKeys.START_BUTTON);
        this.charactersTexture = resources.getTexture(AssetKeys.CHARS);
    }

    public void init(RenderWindow window) {

        backgroundSprite = new Sprite(backgroundTexture);

        float scaleX = 1920f / backgroundTexture.getSize().x;
        float scaleY = 1080f / backgroundTexture.getSize().y;
        backgroundSprite.setScale(scaleX, scaleY);

        startButtonSprite = new Sprite(startButtonTexture);

        FloatRect startBounds = startButtonSprite.getLocalBounds();
        startButtonSprite.setPosition(
                960f - startBounds.width * 0.5f,
                500f - startBounds.height * 0.5f
        );

        initCharacterButtons();
    }

    private void initCharacterButtons() {

        characterButtons = new Sprite[CHARACTER_COUNT];

        float spacing = 220f;
        float totalWidth = (CHARACTER_COUNT - 1) * spacing;
        float startX = 960f - totalWidth * 0.5f;
        float y = 700f;

        for (int i = 0; i < CHARACTER_COUNT; i++) {

            Sprite sprite = new Sprite(charactersTexture);

            int charIndex = i;

            int charRowBlock = charIndex * CHAR_ROWS;

            int textureX = 0;
            int textureY = charRowBlock * FRAME_SIZE;

            sprite.setTextureRect(new IntRect(
                    textureX,
                    textureY,
                    FRAME_SIZE,
                    FRAME_SIZE
            ));

            sprite.setPosition(startX + i * spacing, y);
            sprite.setScale(2f, 2f);

            characterButtons[i] = sprite;
        }
    }

    public int getSelectedCharacterId() {
        return selectedCharacterId;
    }

    public void handleEvent(Event event) {

        if (event.type == Event.Type.MOUSE_MOVED) {

            float mx = event.asMouseEvent().position.x;
            float my = event.asMouseEvent().position.y;

            startHover = startButtonSprite
                    .getGlobalBounds()
                    .contains(mx, my);
        }

        if (event.type == Event.Type.MOUSE_BUTTON_PRESSED) {

            float mx = event.asMouseButtonEvent().position.x;
            float my = event.asMouseButtonEvent().position.y;

            for (int i = 0; i < characterButtons.length; i++) {
                if (characterButtons[i]
                        .getGlobalBounds()
                        .contains(mx, my)) {

                    selectedCharacterId = i;
                }
            }

            if (startButtonSprite
                    .getGlobalBounds()
                    .contains(mx, my)) {

                onStart.run();
            }
        }
    }

    public void render(RenderWindow window) {

        window.clear(Color.BLACK);

        window.draw(backgroundSprite);

        for (int i = 0; i < characterButtons.length; i++) {

            if (i == selectedCharacterId) {
                characterButtons[i].setColor(Color.WHITE);
            } else {
                characterButtons[i].setColor(
                        new Color(255, 255, 255, 150)
                );
            }

            window.draw(characterButtons[i]);
        }

        if (startHover) {
            startButtonSprite.setColor(
                    new Color(255, 255, 255, 220)
            );
        } else {
            startButtonSprite.setColor(Color.WHITE);
        }

        window.draw(startButtonSprite);

        window.display();
    }

    public void shutdown() {
    }
}