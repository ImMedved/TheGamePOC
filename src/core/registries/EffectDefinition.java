package core.registries;

import core.states.EffectCategory;

import java.util.HashMap;
import java.util.Map;

public final class EffectDefinition {

    public final int id;

    public EffectCategory category;

    public boolean stackable;
    public boolean refreshOnReapply;

    public final Map<String, Object> basePayload;

    public EffectDefinition(int id) {
        this.id = id;
        this.basePayload = new HashMap<>();
    }
}