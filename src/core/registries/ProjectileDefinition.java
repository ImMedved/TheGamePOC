package core.registries;

import java.util.HashMap;
import java.util.Map;

public final class ProjectileDefinition {

    public final int id;

    public float speed;
    public float baseDamage;
    public float lifetime;
    public float hitboxRadius;

    public final Map<String, Object> attributes;

    public ProjectileDefinition(int id) {
        this.id = id;
        this.attributes = new HashMap<>();
    }
}