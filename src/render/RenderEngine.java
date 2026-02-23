package render;

import core.PlayerState;
import core.ProjectileState;
import core.WorldState;
import core.WorldStateProvider;
import input.InputModule;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;
import org.jsfml.window.ContextActivationException;
import org.jsfml.window.VideoMode;
import org.jsfml.window.WindowStyle;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class RenderEngine {

    private RenderWindow window;
    private final WorldStateProvider worldProvider;
    private final InputModule inputModule;

    private Thread renderThread;
    private volatile boolean running = false;

    private final CircleShape localPlayerShape;
    private final CircleShape remotePlayerShape;
    private final CircleShape projectileShape;

    private static final int TILE_SIZE = 100;
    private static final int TILES_X = 20;
    private static final int TILES_Y = 20;

    private Texture tileTexture;
    private Texture wallTexture;
    private RectangleShape[][] tiles;

    private View worldView;

    private Thread cameraThread;
    private volatile boolean cameraRunning = false;

    private volatile float cameraX;
    private volatile float cameraY;

    private static final float CAMERA_LERP = 0.08f; // при фризах можно ставить 0,05f, в целом даже 0.04 работает еще ок, ниже нет смысла.
    private static final float DEAD_ZONE_PERCENT = 0.2f;


    public RenderEngine(WorldStateProvider worldProvider, InputModule inputModule) {
        this.worldProvider = worldProvider;
        this.inputModule = inputModule;

        this.localPlayerShape = new CircleShape();
        this.localPlayerShape.setFillColor(Color.GREEN);

        this.remotePlayerShape = new CircleShape();
        this.remotePlayerShape.setFillColor(Color.RED);

        this.projectileShape = new CircleShape(4f);
        this.projectileShape.setFillColor(Color.WHITE);

        // рисуем сетку текстур, где tile.png это 200х200, всего 20х20 тайлов
        tileTexture = new Texture();
        try {
            tileTexture.loadFromFile(Paths.get("assets/tile.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        wallTexture = new Texture();
        try {
            wallTexture.loadFromFile(Paths.get("assets/wallTile.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tiles = new RectangleShape[TILES_X][TILES_Y];

        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {

                RectangleShape tile = new RectangleShape(
                        new Vector2f(TILE_SIZE, TILE_SIZE)
                );

                boolean isWall =
                        x == 0 || y == 0 ||
                                x == TILES_X - 1 ||
                                y == TILES_Y - 1;

                tile.setTexture(isWall ? wallTexture : tileTexture);
                tile.setPosition(x * TILE_SIZE, y * TILE_SIZE);

                tiles[x][y] = tile;
            }
        }
    }

    public void start() {
        running = true;

        renderThread = new Thread(() -> {
            System.out.println("Render thread started");
            renderLoop();
        });

        renderThread.setName("RenderThread");
        renderThread.start();
    }

    public void stop() {
        running = false;
        if (renderThread != null) {
            try {
                renderThread.join();
            } catch (InterruptedException ignored) {
            }
        }
        cameraRunning = false;
        if (cameraThread != null) {
            try {
                cameraThread.join();
            } catch (InterruptedException ignored) {}
        }

    }

    private void renderLoop() {

        System.out.println("Entered renderLoop");

        window = new RenderWindow(
                new VideoMode(1920, 1080),
                "Game",
                WindowStyle.FULLSCREEN
        );

        try {
            window.setActive(true);
        } catch (ContextActivationException e) {
            e.printStackTrace();
            return;
        }

        worldView = new View(
                new FloatRect(
                        0, 0,
                        window.getSize().x,
                        window.getSize().y
                )
        );

        cameraX = window.getSize().x / 2f;
        cameraY = window.getSize().y / 2f;

        cameraRunning = true;
        cameraThread = new Thread(this::cameraLoop);
        cameraThread.setName("CameraThread");
        cameraThread.start();

        //System.out.println("Entered renderLoop; window.isOpen=" + window.isOpen());

        // плавная скользящая камера
        worldView = new View(
                new FloatRect(0, 0,
                        window.getSize().x,
                        window.getSize().y)
        );

        cameraX = window.getSize().x / 2f;
        cameraY = window.getSize().y / 2f;

        while (running && window.isOpen()) {

            // Печать наличия фокуса (если метод доступен), иначе обработаем фокус-ev
            try {
                // Некоторым платформам/версии JSFML может не иметь hasFocus; обёртка на случай отсутствия
                boolean hasFocus = false;
                try {
                    // reflectively or direct call if available
                    hasFocus = (boolean) RenderWindow.class.getMethod("hasFocus").invoke(window);
                } catch (NoSuchMethodException nsme) {
                    // ignore, will rely on focus events
                }
                //System.out.println("Frame start; window.hasFocus=" + hasFocus);
            } catch (Exception ignored) {}

            for (org.jsfml.window.event.Event event : window.pollEvents()) {
                //System.out.println("EVENT: " + event.type);

                // Доп. распечатать ключ/кнопку, если это KEY_*/MOUSE_*
                switch (event.type) {
                    case KEY_PRESSED:
                        //System.out.println("  KEY_PRESSED -> " + event.asKeyEvent().key);
                        break;
                    case KEY_RELEASED:
                        //System.out.println("  KEY_RELEASED -> " + event.asKeyEvent().key);
                        break;
                    case MOUSE_BUTTON_PRESSED:
                        //System.out.println("  MOUSE_PRESSED -> " + event.asMouseButtonEvent().button
                        //        + " pos=" + event.asMouseButtonEvent().position);
                        break;
                    case MOUSE_BUTTON_RELEASED:
                        //System.out.println("  MOUSE_RELEASED -> " + event.asMouseButtonEvent().button
                        //        + " pos=" + event.asMouseButtonEvent().position);
                        break;
                    case MOUSE_MOVED:
                        //System.out.println("  MOUSE_MOVED -> pos=" + event.asMouseEvent().position);
                        break;
                    case GAINED_FOCUS:
                        //System.out.println("  GAINED_FOCUS");
                        break;
                    case LOST_FOCUS:
                        //System.out.println("  LOST_FOCUS");
                        break;
                    default:
                        break;
                }

                if (event.type == org.jsfml.window.event.Event.Type.CLOSED) {
                    window.close();
                }

                inputModule.handleEvent(event);
            }
            //System.out.println("LiveInputState: " + inputModule.debugLiveState());

            // Также печатаем последний snapshot (immutable)
            // System.out.println("Latest Snapshot (render): " + inputModule.getLatestSnapshot());

            // ОБРАБОТКА СОБЫТИЙ КАЖДЫЙ КАДР
            /*for (org.jsfml.window.event.Event event : window.pollEvents()) {

                //System.out.println("Event: " + event.type);

                if (event.type == org.jsfml.window.event.Event.Type.CLOSED) {
                    window.close();
                }

                inputModule.handleEvent(event);
            }*/ //дубликат

            // Вывод snapshot каждый кадр
            // System.out.println("Input snapshot (render side): "
            //        + inputModule.getLatestSnapshot());

            WorldState state = worldProvider.getLatestWorldState();

            window.clear(Color.BLACK);

            worldView.setCenter(cameraX, cameraY);
            window.setView(worldView);
            // System.out.println("Player pos X: " + state.localPlayer.x + ". Player pos Y: " +
            //         state.localPlayer.y + ". Camera pos X: " + cameraX + ". Camera pos Y: " + cameraY);
            drawLevel();

            window.setView(window.getDefaultView());

            drawPlayer(state.localPlayer, localPlayerShape);
            drawPlayer(state.remotePlayer, remotePlayerShape);
            drawProjectiles(state.projectiles);

            drawPlayer(state.localPlayer, localPlayerShape);
            drawPlayer(state.remotePlayer, remotePlayerShape);

            drawProjectiles(state.projectiles);

            if (state.gameOver) {
                drawGameOver(state.winnerId);
            }

            window.display();

            try {
                Thread.sleep(10); // 4 тика в секунду, поправить TODO
            } catch (InterruptedException ignored) {}
        }
        window.setView(window.getDefaultView()); // тот же вопрос, точно это тут?
    }

    private void cameraLoop() {

        long lastTime = System.nanoTime();

        while (cameraRunning) {

            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            WorldState state = worldProvider.getLatestWorldState();
            PlayerState player = state.localPlayer;

            float viewWidth = worldView.getSize().x;
            float viewHeight = worldView.getSize().y;

            float halfW = viewWidth / 2f;
            float halfH = viewHeight / 2f;

            float deadW = viewWidth * DEAD_ZONE_PERCENT;
            float deadH = viewHeight * DEAD_ZONE_PERCENT;

            float deadLeft = cameraX - deadW / 2f;
            float deadRight = cameraX + deadW / 2f;
            float deadTop = cameraY - deadH / 2f;
            float deadBottom = cameraY + deadH / 2f;

            float targetX = cameraX;
            float targetY = cameraY;

            if (player.x < deadLeft) {
                targetX = player.x + deadW / 2f;
            } else if (player.x > deadRight) {
                targetX = player.x - deadW / 2f;
            }

            if (player.y < deadTop) {
                targetY = player.y + deadH / 2f;
            } else if (player.y > deadBottom) {
                targetY = player.y - deadH / 2f;
            }

            float lerpFactor = 10f * dt;  // скорость камеры (регулируется)

            cameraX += (targetX - cameraX) * lerpFactor;
            cameraY += (targetY - cameraY) * lerpFactor;

            float worldMinX = 0;
            float worldMinY = 0;
            float worldMaxX = state.getWorldWidth();
            float worldMaxY = state.getWorldHeight();

            cameraX = clamp(cameraX, worldMinX + halfW, worldMaxX - halfW);
            cameraY = clamp(cameraY, worldMinY + halfH, worldMaxY - halfH);
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }


    private void drawPlayer(PlayerState player, CircleShape shape) {

        shape.setRadius(player.hitboxRadius);
        shape.setOrigin(player.hitboxRadius, player.hitboxRadius);
        shape.setPosition(player.x, player.y);

        window.draw(shape);
    }

    private void drawProjectiles(List<ProjectileState> projectiles) {

        for (ProjectileState p : projectiles) {
            projectileShape.setPosition(p.x, p.y);
            window.draw(projectileShape);
        }
    }

    private void drawGameOver(int winnerId) {

        RectangleShape overlay = new RectangleShape(
                new Vector2f(window.getSize().x, window.getSize().y)
        );
        overlay.setFillColor(new Color(0, 0, 0, 150));

        window.draw(overlay);
    }

    private void drawLevel() {

        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {
                window.draw(tiles[x][y]);
            }
        }
    }

}

