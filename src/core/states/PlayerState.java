package core.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerState {

    public long id;
    public int characterId;

    public Vector2 position;
    public Vector2 previousPosition;
    public Vector2 velocity;

    public float rotation;

    public float health;
    public float maxHealth;

    public float hitboxRadius;
    public boolean alive;

    public Map<String, Object> attributes;
    public Set<String> tags;

    public PlayerState(long id) {
        this.id = id;
        this.position = new Vector2();
        this.previousPosition = new Vector2();
        this.velocity = new Vector2();
        this.attributes = new HashMap<>();
        this.tags = new HashSet<>();
        this.alive = true;
    }

    public PlayerState copy() {
        PlayerState copy = new PlayerState(this.id);
        copy.characterId = this.characterId;

        copy.position = this.position.copy();
        copy.previousPosition = this.previousPosition.copy();
        copy.velocity = this.velocity.copy();

        copy.rotation = this.rotation;
        copy.health = this.health;
        copy.maxHealth = this.maxHealth;
        copy.hitboxRadius = this.hitboxRadius;
        copy.alive = this.alive;

        copy.attributes = new HashMap<>(this.attributes);
        copy.tags = new HashSet<>(this.tags);

        return copy;
    }
}