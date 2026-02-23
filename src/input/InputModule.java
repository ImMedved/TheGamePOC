package input;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.window.event.Event;
import org.jsfml.window.Mouse;
import org.jsfml.window.Keyboard;

import java.util.concurrent.atomic.AtomicReference;

public class InputModule {

    private final LiveInputState liveState = new LiveInputState();

    private final AtomicReference<InputSnapshot> latestSnapshot =
            new AtomicReference<>(new InputSnapshot(0, 0f, 0f, false, 0f, 0f));

    private volatile boolean running = false; // почему это поле не используется? Где оно вообще было?
    // Вроде бы это состояние в кор движке в тик луп, а не в инпуте. ???

    private volatile int tickCounter = 0;

    public InputModule() {}

    public void handleEvent(org.jsfml.window.event.Event event) {

        switch (event.type) {

            case KEY_PRESSED:
                handleKeyPressed(event.asKeyEvent().key);
                break;

            case KEY_RELEASED:
                handleKeyReleased(event.asKeyEvent().key);
                break;

            case MOUSE_BUTTON_PRESSED:
                if (event.asMouseButtonEvent().button == org.jsfml.window.Mouse.Button.LEFT) {
                    liveState.lmbDown = true;
                }
                break;

            case MOUSE_BUTTON_RELEASED:
                if (event.asMouseButtonEvent().button == org.jsfml.window.Mouse.Button.LEFT) {
                    liveState.lmbDown = false;
                }
                break;

            case MOUSE_MOVED:
                liveState.mouseX = event.asMouseEvent().position.x;
                liveState.mouseY = event.asMouseEvent().position.y;
                break;

            default:
                break;
        }
    }

    private void handleKeyPressed(Keyboard.Key key) {
        if (key == Keyboard.Key.W) liveState.wDown = true;
        if (key == Keyboard.Key.A) liveState.aDown = true;
        if (key == Keyboard.Key.S) liveState.sDown = true;
        if (key == Keyboard.Key.D) liveState.dDown = true;
    }

    private void handleKeyReleased(Keyboard.Key key) {
        if (key == Keyboard.Key.W) liveState.wDown = false;
        if (key == Keyboard.Key.A) liveState.aDown = false;
        if (key == Keyboard.Key.S) liveState.sDown = false;
        if (key == Keyboard.Key.D) liveState.dDown = false;
    }

    public void publishSnapshot(int tick) {

        float moveX = 0f;
        float moveY = 0f;

        if (liveState.wDown) moveY -= 1f;
        if (liveState.sDown) moveY += 1f;
        if (liveState.aDown) moveX -= 1f;
        if (liveState.dDown) moveX += 1f;

        if (moveX != 0f || moveY != 0f) {
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        boolean shoot = liveState.lmbDown && !liveState.prevLmbDown;
        liveState.prevLmbDown = liveState.lmbDown;

        InputSnapshot snapshot = new InputSnapshot(
                tick,
                moveX,
                moveY,
                shoot,
                liveState.mouseX,
                liveState.mouseY
        );

        latestSnapshot.set(snapshot);
        tickCounter = tick;
    }

    public InputSnapshot getLatestSnapshot() {
        return latestSnapshot.get();
    }

    public int getLatestTick() {
        return tickCounter;
    }

    public String debugLiveState() {
        return "w=" + liveState.wDown +
                " a=" + liveState.aDown +
                " s=" + liveState.sDown +
                " d=" + liveState.dDown +
                " lmb=" + liveState.lmbDown +
                " prevLmb=" + liveState.prevLmbDown +
                " mouse=(" + liveState.mouseX + "," + liveState.mouseY + ")";
    }

}
