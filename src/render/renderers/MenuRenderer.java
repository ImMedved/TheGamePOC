package render.renderers;

import org.jsfml.graphics.*;
import org.jsfml.window.event.Event;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import java.util.function.Consumer;

public final class MenuRenderer {

    private static final float WINDOW_WIDTH = 1280f;
    private static final float WINDOW_HEIGHT = 720f;

    private final Runnable onStart;
    private final Consumer<Integer> onCharacterSelected;

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

    public MenuRenderer(ResourceManager resources,
                        Runnable onStart,
                        Consumer<Integer> onCharacterSelected) {
        this.onStart = onStart;
        this.onCharacterSelected = onCharacterSelected;
        this.backgroundTexture = resources.getTexture(AssetKeys.BACKGROUND);
        this.startButtonTexture = resources.getTexture(AssetKeys.START_BUTTON);
        this.charactersTexture = resources.getTexture(AssetKeys.CHARS);
    }

    public void init(RenderWindow window) {

        backgroundSprite = new Sprite(backgroundTexture);

        float scaleX = WINDOW_WIDTH / backgroundTexture.getSize().x;
        float scaleY = WINDOW_HEIGHT / backgroundTexture.getSize().y;
        backgroundSprite.setScale(scaleX, scaleY);

        startButtonSprite = new Sprite(startButtonTexture);

        FloatRect startBounds = startButtonSprite.getLocalBounds();
        startButtonSprite.setPosition(
                WINDOW_WIDTH * 0.5f - startBounds.width * 0.5f,
                320f - startBounds.height * 0.5f
        );

        initCharacterButtons();
    }

    private void initCharacterButtons() {

        characterButtons = new Sprite[CHARACTER_COUNT];

        float spacing = 220f;
        float totalWidth = (CHARACTER_COUNT - 1) * spacing;
        float startX = WINDOW_WIDTH * 0.5f - totalWidth * 0.5f;
        float y = 500f;

        for (int charIndex = 0; charIndex < CHARACTER_COUNT; charIndex++) {

            Sprite sprite = new Sprite(charactersTexture);

            int charRowBlock = charIndex * CHAR_ROWS;

            int textureX = 0;
            int textureY = charRowBlock * FRAME_SIZE;

            sprite.setTextureRect(new IntRect(
                    textureX,
                    textureY,
                    FRAME_SIZE,
                    FRAME_SIZE
            ));

            sprite.setPosition(startX + charIndex * spacing, y);
            sprite.setScale(2f, 2f);

            characterButtons[charIndex] = sprite;
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

                    if (selectedCharacterId != i) {
                        selectedCharacterId = i;
                        onCharacterSelected.accept(selectedCharacterId);
                    }
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
