package render.renderers;

import core.render.RenderPlayer;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Mouse;
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

    public HudRenderer(ResourceManager resources) {
        bulletTexture = resources.getTexture(AssetKeys.BULLET);
        abilitiesTexture = resources.getTexture(AssetKeys.ABILITIES);
    }

    public void render(RenderWindow window,
                       List<RenderPlayer> players,
                       long localPlayerId) {

        if (players.isEmpty()) return;

        RenderPlayer player = players.get(Math.toIntExact(localPlayerId)-1);

        renderCursor(window, player);
        renderHealth(window, player);
        renderAbilities(window, player);
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

        float x = 1920f * 0.5f - width * 0.5f;
        float y = 960f;

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

        float startX = 1920f * 0.5f - spacing;
        float y = 1000f;

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