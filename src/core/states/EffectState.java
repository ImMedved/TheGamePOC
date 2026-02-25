package core.states;

import core.states.helpers.EffectType;
import core.states.helpers.ParentType;

public final class EffectState {

    public final int effectId;
    public final EffectType type;

    // родитель
    public final int parentId;
    public final ParentType parentType;

    public final float x;
    public final float y;

    public final float lifetime;
    public final float maxLifetime;

    public EffectState(
            int effectId,
            EffectType type,
            int parentId,
            ParentType parentType,
            float x,
            float y,
            float lifetime,
            float maxLifetime
    ) {
        this.effectId = effectId;
        this.type = type;
        this.parentId = parentId;
        this.parentType = parentType;
        this.x = x;
        this.y = y;
        this.lifetime = lifetime;
        this.maxLifetime = maxLifetime;
    }

    public EffectState withLifetime(float newLifetime) {
        return new EffectState(
                effectId,
                type,
                parentId,
                parentType,
                x,
                y,
                newLifetime,
                maxLifetime
        );
    }
}