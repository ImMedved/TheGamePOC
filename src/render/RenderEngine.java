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

    public RenderEngine(WorldStateProvider worldProvider, InputModule inputModule) {
        this.worldProvider = worldProvider;
        this.inputModule = inputModule;

        this.localPlayerShape = new CircleShape();
        this.localPlayerShape.setFillColor(Color.GREEN);

        this.remotePlayerShape = new CircleShape();
        this.remotePlayerShape.setFillColor(Color.RED);

        this.projectileShape = new CircleShape(4f);
        this.projectileShape.setFillColor(Color.WHITE);
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
    }

    private void renderLoop() {

        System.out.println("Entered renderLoop");

        window = new RenderWindow(
                new VideoMode(800, 600),
                "Game",
                WindowStyle.DEFAULT
        );

        try {
            window.setActive(true);
        } catch (ContextActivationException e) {
            e.printStackTrace();
            return;
        }
        //System.out.println("Entered renderLoop; window.isOpen=" + window.isOpen());

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
            //System.out.println("Latest Snapshot (render): " + inputModule.getLatestSnapshot());

            // ОБРАБОТКА СОБЫТИЙ КАЖДЫЙ КАДР
            for (org.jsfml.window.event.Event event : window.pollEvents()) {

                //System.out.println("Event: " + event.type);

                if (event.type == org.jsfml.window.event.Event.Type.CLOSED) {
                    window.close();
                }

                inputModule.handleEvent(event);
            }

            // Вывод snapshot каждый кадр
            //System.out.println("Input snapshot (render side): "
            //        + inputModule.getLatestSnapshot());

            WorldState state = worldProvider.getLatestWorldState();

            window.clear(Color.BLACK);

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
}

