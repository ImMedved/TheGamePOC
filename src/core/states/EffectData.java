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

    public int stackCount;

    public Map<String, Object> payload;
    public Set<String> tags;

    public EffectData(long id) {
        this.id = id;
        this.payload = new HashMap<>();
        this.tags = new HashSet<>();
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

        copy.stackCount = this.stackCount;

        copy.payload = new HashMap<>(this.payload);
        copy.tags = new HashSet<>(this.tags);

        return copy;
    }
}