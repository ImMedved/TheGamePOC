package core.factories;

import core.states.EffectData;
import core.states.EffectCategory;
import core.states.Vector2;

public final class EffectFactory {

    public static EffectData createBulletHole(
            long id,
            int effectTypeId,
            float x,
            float y
    ) {
        EffectData effect = new EffectData(id);

        effect.effectTypeId = effectTypeId;
        effect.category = EffectCategory.VISUAL;

        effect.position.set(x, y);
        effect.previousPosition.set(x, y);

        effect.duration = 3f;
        effect.elapsed = 0f;

        effect.tickInterval = 0f;
        effect.tickAccumulator = 0f;

        return effect;
    }

    public static EffectData createDashEffect(
            long id,
            float x,
            float y,
            float rotation,
            float length
    ) {
        EffectData e = new EffectData(id);

        e.effectTypeId = EffectType.DASH;
        e.position.set(x, y);

        e.duration = 1f;
        e.elapsed = 0f;

        e.rotation = rotation;
        e.length = length;

        e.category = EffectCategory.HYBRID;

        return e;
    }
}