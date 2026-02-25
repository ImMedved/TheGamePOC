package core.states.events;

import core.states.helpers.Vector2;

import java.util.HashMap;
import java.util.Map;

public final class GameEvent {

    public EventType type;
    public final Vector2 position = new Vector2();
    public int sourceId;

    public final Map<String, Object> payload = new HashMap<>();

    public GameEvent(EventType type) {
        this.type = type;
    }
}