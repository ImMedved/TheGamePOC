package core.states;

import core.states.helpers.Vector2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerState {

    public final int id;

    public final Vector2 position = new Vector2();
    public final Vector2 previousPosition = new Vector2();
    public final Vector2 velocity = new Vector2();

    public float rotation;
    public float health;

    public final Map<String, Object> attributes = new HashMap<>();
    public final Set<String> tags = new HashSet<>();

    public PlayerState(int id) {
        this.id = id;
    }
}