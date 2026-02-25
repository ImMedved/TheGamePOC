package core.states.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EffectData {

    public long id;

    public EffectType type;
    public EffectCategory category;

    public int sourceId;
    public int targetId;

    public float duration;
    public float elapsed;

    public float tickInterval;
    public float tickAccumulator;

    public final Map<String, Object> payload = new HashMap<>();
    public final Set<String> tags = new HashSet<>();
}
