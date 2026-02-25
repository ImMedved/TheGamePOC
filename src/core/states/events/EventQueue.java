package core.states.events;

import java.util.ArrayList;
import java.util.List;

public final class EventQueue {

    private final List<GameEvent> events = new ArrayList<>();

    public void add(GameEvent event) {
        events.add(event);
    }

    public List<GameEvent> all() {
        return events;
    }

    public void clear() {
        events.clear();
    }
}