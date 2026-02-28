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
}