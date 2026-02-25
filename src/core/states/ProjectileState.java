package core.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProjectileState {

    public long id;
    public long ownerId;
    public int projectileTypeId;

    public Vector2 position;
    public Vector2 previousPosition;
    public Vector2 velocity;

    public float lifetime;
    public float elapsed;

    public float hitboxRadius;
    public float damage;

    public Map<String, Object> attributes;
    public Set<String> tags;

    public ProjectileState(long id) {
        this.id = id;
        this.position = new Vector2();
        this.previousPosition = new Vector2();
        this.velocity = new Vector2();
        this.attributes = new HashMap<>();
        this.tags = new HashSet<>();
    }

    public ProjectileState copy() {
        ProjectileState copy = new ProjectileState(this.id);

        copy.ownerId = this.ownerId;
        copy.projectileTypeId = this.projectileTypeId;

        copy.position = this.position.copy();
        copy.previousPosition = this.previousPosition.copy();
        copy.velocity = this.velocity.copy();

        copy.lifetime = this.lifetime;
        copy.elapsed = this.elapsed;

        copy.hitboxRadius = this.hitboxRadius;
        copy.damage = this.damage;

        copy.attributes = new HashMap<>(this.attributes);
        copy.tags = new HashSet<>(this.tags);

        return copy;
    }
}