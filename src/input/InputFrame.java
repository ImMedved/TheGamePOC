package input;

import java.util.HashMap;
import java.util.Map;

public final class InputFrame {

    public final int tick;

    private final Map<Long, InputSnapshot> inputs = new HashMap<>();

    public InputFrame(int tick) {
        this.tick = tick;
    }

    public void put(InputSnapshot snapshot) {
        System.out.println("[FRAME] put player=" + snapshot.ownerId +
                " moveX=" + snapshot.moveX +
                " tick=" + snapshot.tick);
        inputs.put(snapshot.ownerId, snapshot);
    }

    public InputSnapshot get(long playerId) {
        return inputs.get(playerId);
    }

    public Map<Long, InputSnapshot> all() {
        return inputs;
    }
}