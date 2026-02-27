package core.systems;

import core.SimulationContext;
import core.commands.ApplyEffectCommand;
import core.commands.RemoveEffectCommand;
import core.registries.EffectDefinition;
import core.registries.EffectRegistry;
import core.states.EffectData;

public final class EffectTickSystem implements GameSystem {

    private final EffectRegistry registry;

    public EffectTickSystem(EffectRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        for (EffectData effect : context.snapshot().effects) {

            effect.elapsed += context.dt();
            effect.tickAccumulator += context.dt();

            EffectDefinition def = registry.get(effect.effectTypeId);

            if (effect.tickInterval > 0f && effect.tickAccumulator >= effect.tickInterval) {

                effect.tickAccumulator = 0f;

                context.addCommand(new ApplyEffectCommand(effect.copy())
                );
            }

            if (effect.elapsed >= effect.duration) {
                context.addCommand(new RemoveEffectCommand(effect.id)
                );
            }
        }
    }
}