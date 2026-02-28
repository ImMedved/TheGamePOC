package core.config;

import core.registries.EffectDefinition;
import core.registries.EffectRegistry;
import core.states.EffectCategory;

public final class EffectConfigs {

    public static final int BULLET_HOLE = 0;

    public static void registerAll(EffectRegistry registry) {

        EffectDefinition hole = new EffectDefinition(BULLET_HOLE);

        hole.category = EffectCategory.VISUAL;
        hole.stackable = false;
        hole.refreshOnReapply = false;

        registry.register(hole);
    }
}