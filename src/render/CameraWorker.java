package render;

import core.PlayerState;
import core.WorldState;
import core.WorldStateProvider;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.View;

public class CameraWorker {

    private final WorldStateProvider worldProvider;

    private Thread cameraThread;
    private volatile boolean running = false;

    private volatile float cameraX;
    private volatile float cameraY;

    private View view;

    private static final float DEAD_ZONE_PERCENT = 0.2f;

    public CameraWorker(WorldStateProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    public void start(RenderWindow window) {

        view = new View(
                new FloatRect(0, 0,
                        window.getSize().x,
                        window.getSize().y)
        );

        cameraX = window.getSize().x / 2f;
        cameraY = window.getSize().y / 2f;

        running = true;

        cameraThread = new Thread(this::loop);
        cameraThread.setName("CameraThread");
        cameraThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (cameraThread != null) cameraThread.join();
        } catch (InterruptedException ignored) {}
    }

    public View getView() {
        view.setCenter(cameraX, cameraY);
        return view;
    }

    private void loop() {

        long last = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
            float dt = (now - last) / 1_000_000_000f;
            last = now;

            WorldState state = worldProvider.getLatestWorldState();
            PlayerState player = state.localPlayer;

            float viewWidth = view.getSize().x;
            float viewHeight = view.getSize().y;

            float halfW = viewWidth / 2f;
            float halfH = viewHeight / 2f;

            float deadW = viewWidth * DEAD_ZONE_PERCENT;
            float deadH = viewHeight * DEAD_ZONE_PERCENT;

            float targetX = cameraX;
            float targetY = cameraY;

            if (player.x < cameraX - deadW / 2f)
                targetX = player.x + deadW / 2f;

            if (player.x > cameraX + deadW / 2f)
                targetX = player.x - deadW / 2f;

            if (player.y < cameraY - deadH / 2f)
                targetY = player.y + deadH / 2f;

            if (player.y > cameraY + deadH / 2f)
                targetY = player.y - deadH / 2f;

            float speed = 6f;
            cameraX += (targetX - cameraX) * speed * dt;
            cameraY += (targetY - cameraY) * speed * dt;

            float worldMaxX = state.getWorldWidth();
            float worldMaxY = state.getWorldHeight();

            cameraX = clamp(cameraX, halfW, worldMaxX - halfW);
            cameraY = clamp(cameraY, halfH, worldMaxY - halfH);
        }
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}