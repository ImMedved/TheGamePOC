package core.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EffectData {

    public long id;
    public int effectTypeId;

    public EffectCategory category;

    public long sourceId;
    public long targetId;

    public float duration;
    public float elapsed;

    public float tickInterval;
    public float tickAccumulator;

    public Vector2 position;
    public Vector2 previousPosition;

    public int stackCount;

    public Map<String, Object> payload;
    public Set<String> tags;

    public EffectType type;

    public float rotation;   // radians
    public float scaleX = 1f;
    public float scaleY = 1f;

    public float dx;
    public float dy;

    public EffectData(long id) {
        this.id = id;
        this.payload = new HashMap<>();
        this.tags = new HashSet<>();
        this.position = new Vector2();
        this.previousPosition = new Vector2();
    }

    public EffectData copy() {
        EffectData copy = new EffectData(this.id);

        copy.effectTypeId = this.effectTypeId;
        copy.category = this.category;

        copy.sourceId = this.sourceId;
        copy.targetId = this.targetId;

        copy.duration = this.duration;
        copy.elapsed = this.elapsed;

        copy.tickInterval = this.tickInterval;
        copy.tickAccumulator = this.tickAccumulator;

        copy.position = this.position.copy();
        copy.previousPosition = this.previousPosition.copy();

        copy.stackCount = this.stackCount;

        copy.payload = new HashMap<>(this.payload);
        copy.tags = new HashSet<>(this.tags);

        copy.rotation = this.rotation;
        copy.scaleX = this.scaleX;
        copy.scaleY = this.scaleY;
        copy.type = this.type;

        copy.dx = this.dx;
        copy.dy = this.dy;
        return copy;
    }
}