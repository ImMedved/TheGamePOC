package core.registries;

import java.util.HashMap;
import java.util.Map;

public final class CharacterDefinition {

    public final int id;

    public float baseSpeed;
    public float baseHealth;
    public float baseHitboxRadius;

    public final Map<String, Object> attributes;

    public CharacterDefinition(int id) {
        this.id = id;
        this.attributes = new HashMap<>();
    }
}