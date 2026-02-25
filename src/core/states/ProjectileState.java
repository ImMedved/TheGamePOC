package core.states;

import core.states.helpers.Vector2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProjectileState {

    public final int id;
    public final int ownerId;

    public final Vector2 position = new Vector2();
    public final Vector2 previousPosition = new Vector2();
    public final Vector2 velocity = new Vector2();

    public float lifetime;
    public float elapsed;
    public float damage;

    public final Map<String, Object> attributes = new HashMap<>();
    public final Set<String> tags = new HashSet<>();

    public ProjectileState(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }
}