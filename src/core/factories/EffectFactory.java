package core.factories;

import core.states.EffectData;
import core.states.EffectCategory;
import core.states.EffectType;
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
        effect.type = EffectType.BULLET_HOLE;

        effect.position.set(x, y);
        effect.previousPosition.set(x, y);

        effect.duration = 3f;
        effect.elapsed = 0f;

        effect.tickInterval = 0f;
        effect.tickAccumulator = 0f;

        return effect;
    }

    public static EffectData createSpeedEffect(
            long id,
            long targetId
    ) {
        EffectData e = new EffectData(id);

        e.type = EffectType.SPEED_AURA;
        e.category = EffectCategory.VISUAL;

        e.targetId = targetId;

        e.duration = 2f;
        e.elapsed = 0f;

        return e;
    }

    public static EffectData createDashEffect(
            long id,
            float startX,
            float startY,
            float dx,
            float dy
    ) {
        EffectData e = new EffectData(id);

        e.type = EffectType.DASH_TRACE;
        e.category = EffectCategory.HYBRID;

        e.position.set(startX, startY);
        e.previousPosition.set(startX, startY);

        e.dx = dx;
        e.dy = dy;

        e.duration = 0.25f;   // короткий трейл
        e.elapsed = 0f;

        return e;
    }
}