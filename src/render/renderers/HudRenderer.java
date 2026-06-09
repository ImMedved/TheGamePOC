package render.renderers;

import core.render.RenderPlayer;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Mouse;
import render.Camera;
import render.resources.AssetKeys;
import render.resources.ResourceManager;

import java.util.List;

public final class HudRenderer {

    private final Texture bulletTexture;
    private final Texture abilitiesTexture;

    private final CircleShape cooldownCircle = new CircleShape(22f, 60);

    private static final int BULLET_W = 13;
    private static final int BULLET_H = 40;

    private static final int ABILITY_SIZE = 64;
    private static final float PLAYER_BAR_WIDTH = 64f;
    private static final float PLAYER_BAR_HEIGHT = 8f;
    private static final float PLAYER_BAR_OFFSET_Y = 48f;
    private static final float DIGIT_THICKNESS = 2f;

    public HudRenderer(ResourceManager resources) {
        bulletTexture = resources.getTexture(AssetKeys.BULLET);
        abilitiesTexture = resources.getTexture(AssetKeys.ABILITIES);
    }

    public void render(RenderWindow window,
                       List<RenderPlayer> players,
                       Camera camera,
                       long localPlayerId) {

        if (players.isEmpty()) return;

        RenderPlayer player = players.get(Math.toIntExact(localPlayerId)-1);

        renderPlayerBars(window, players, camera, localPlayerId);
        renderCursor(window, player);
        renderHealth(window, player);
        renderAbilities(window, player);
    }

    private void renderPlayerBars(RenderWindow window,
                                  List<RenderPlayer> players,
                                  Camera camera,
                                  long localPlayerId) {

        for (RenderPlayer player : players) {
            float screenX = camera.worldToScreenX(player.currX);
            float screenY = camera.worldToScreenY(player.currY) - PLAYER_BAR_OFFSET_Y;
            float ratio = player.maxHealth <= 0f ? 0f : Math.max(0f, Math.min(1f, player.health / player.maxHealth));

            RectangleShape bg = new RectangleShape(new Vector2f(PLAYER_BAR_WIDTH, PLAYER_BAR_HEIGHT));
            bg.setFillColor(new Color(30, 30, 30, 220));
            bg.setOutlineColor(new Color(0, 0, 0, 220));
            bg.setOutlineThickness(1f);
            bg.setPosition(screenX - PLAYER_BAR_WIDTH * 0.5f, screenY);
            window.draw(bg);

            RectangleShape fill = new RectangleShape(new Vector2f(PLAYER_BAR_WIDTH * ratio, PLAYER_BAR_HEIGHT));
            fill.setFillColor(player.id == localPlayerId ? new Color(60, 210, 90) : new Color(210, 70, 70));
            fill.setPosition(screenX - PLAYER_BAR_WIDTH * 0.5f, screenY);
            window.draw(fill);

            renderLifeCount(window, player.livesRemaining, screenX + PLAYER_BAR_WIDTH * 0.5f + 6f, screenY - 1f);
        }
    }

    private void renderLifeCount(RenderWindow window, int livesRemaining, float x, float y) {
        char[] digits = Integer.toString(Math.max(0, livesRemaining)).toCharArray();
        float cursorX = x;

        for (char digit : digits) {
            renderDigit(window, digit, cursorX, y);
            cursorX += 8f;
        }
    }

    private void renderDigit(RenderWindow window, char digit, float x, float y) {
        boolean[] segments = switch (digit) {
            case '0' -> new boolean[]{true, true, true, true, true, true, false};
            case '1' -> new boolean[]{false, true, true, false, false, false, false};
            case '2' -> new boolean[]{true, true, false, true, true, false, true};
            case '3' -> new boolean[]{true, true, true, true, false, false, true};
            case '4' -> new boolean[]{false, true, true, false, false, true, true};
            case '5' -> new boolean[]{true, false, true, true, false, true, true};
            case '6' -> new boolean[]{true, false, true, true, true, true, true};
            case '7' -> new boolean[]{true, true, true, false, false, false, false};
            case '8' -> new boolean[]{true, true, true, true, true, true, true};
            case '9' -> new boolean[]{true, true, true, true, false, true, true};
            default -> new boolean[]{false, false, false, false, false, false, false};
        };

        Color color = new Color(245, 245, 245);

        if (segments[0]) {
            drawSegment(window, x, y, 4f, DIGIT_THICKNESS, color);
        }
        if (segments[1]) {
            drawSegment(window, x + 4f, y, DIGIT_THICKNESS, 4f, color);
        }
        if (segments[2]) {
            drawSegment(window, x + 4f, y + 4f, DIGIT_THICKNESS, 4f, color);
        }
        if (segments[3]) {
            drawSegment(window, x, y + 8f, 4f, DIGIT_THICKNESS, color);
        }
        if (segments[4]) {
            drawSegment(window, x, y + 4f, DIGIT_THICKNESS, 4f, color);
        }
        if (segments[5]) {
            drawSegment(window, x, y, DIGIT_THICKNESS, 4f, color);
        }
        if (segments[6]) {
            drawSegment(window, x, y + 4f, 4f, DIGIT_THICKNESS, color);
        }
    }

    private void drawSegment(RenderWindow window, float x, float y, float width, float height, Color color) {
        RectangleShape segment = new RectangleShape(new Vector2f(width, height));
        segment.setFillColor(color);
        segment.setPosition(x, y);
        window.draw(segment);
    }

    // CURSOR
    private void renderCursor(RenderWindow window,
                              RenderPlayer player) {

        var mouse = Mouse.getPosition(window);

        float mx = mouse.x;
        float my = mouse.y;

        Sprite cursor = new Sprite(bulletTexture);

        // берём только первый тайл
        cursor.setTextureRect(new IntRect(
                0,
                0,
                BULLET_W,
                BULLET_H
        ));

        cursor.setOrigin(BULLET_W * 0.5f, BULLET_H * 0.5f);
        cursor.setRotation(45f);
        cursor.setPosition(mx, my);

        window.draw(cursor);

        if (player.shootCooldown > 0f) {

            float progress = 1f - player.shootCooldown / 1f;

            drawRadialCooldown(
                    window,
                    mx,
                    my,
                    26f,
                    progress
            );
        }
    }

    // HEALTH BAR
    private void renderHealth(RenderWindow window,
                              RenderPlayer player) {

        float width = 400f;
        float height = 28f;

        float x = window.getSize().x * 0.5f - width * 0.5f;
        float y = window.getSize().y - 100f;

        float ratio = player.health / player.maxHealth;

        RectangleShape bg = new RectangleShape(new Vector2f(width, height));
        bg.setFillColor(new Color(50,50,50));
        bg.setPosition(x, y);

        RectangleShape hp = new RectangleShape(new Vector2f(width * ratio, height));
        hp.setFillColor(new Color(200,50,50));
        hp.setPosition(x, y);

        window.draw(bg);
        window.draw(hp);
    }

    // ABILITIES
    private void renderAbilities(RenderWindow window,
                                 RenderPlayer player) {

        float spacing = 90f;

        float startX = window.getSize().x * 0.5f - spacing;
        float y = window.getSize().y - 68f;

        renderAbility(window,
                startX,
                y,
                0,
                player.characterId,
                player.tripleShotCooldown,
                3f);

        renderAbility(window,
                startX + spacing,
                y,
                1,
                player.characterId,
                player.speedCooldown,
                5f);

        renderAbility(window,
                startX + spacing * 2,
                y,
                2,
                player.characterId,
                player.blinkCooldown,
                7f);
    }

    private void renderAbility(RenderWindow window,
                               float x,
                               float y,
                               int row,
                               int characterId,
                               float cooldown,
                               float maxCooldown) {

        // фон квадрата
        RectangleShape bg = new RectangleShape(new Vector2f(ABILITY_SIZE, ABILITY_SIZE));
        bg.setFillColor(new Color(40,40,40));
        bg.setOutlineColor(new Color(120,120,120));
        bg.setOutlineThickness(3f);
        bg.setPosition(x, y);

        window.draw(bg);

        // иконка
        Sprite icon = new Sprite(abilitiesTexture);

        int u = characterId * ABILITY_SIZE;
        int v = row * ABILITY_SIZE;

        icon.setTextureRect(new IntRect(
                u,
                v,
                ABILITY_SIZE,
                ABILITY_SIZE
        ));

        icon.setPosition(x, y);

        if (cooldown > 0f)
            icon.setColor(new Color(140,140,140));

        window.draw(icon);

        // cooldown circle
        if (cooldown > 0f) {
            if (cooldown < 0.15f) icon.setColor(new Color(255,255,255,255));
            float progress = 1f - cooldown / maxCooldown;

            drawRadialCooldown(
                    window,
                    x + 32f,
                    y + 32f,
                    30f,
                    progress
            );
        }
    }
    private void drawRadialCooldown(RenderWindow window,
                                    float cx,
                                    float cy,
                                    float radius,
                                    float progress) {

        int segments = 40;

        float angle = progress * 360f;

        VertexArray fan = new VertexArray(PrimitiveType.TRIANGLE_FAN);

        fan.add(new Vertex(
                new Vector2f(cx, cy),
                new Color(0,0,0,160)
        ));

        for (int i = 0; i <= segments; i++) {

            float a = (float)Math.toRadians(-90f + angle * (i / (float)segments));

            float x = cx + (float)Math.cos(a) * radius;
            float y = cy + (float)Math.sin(a) * radius;

            fan.add(new Vertex(
                    new Vector2f(x, y),
                    new Color(0,0,0,160)
            ));
        }

        window.draw(fan);
    }
}
