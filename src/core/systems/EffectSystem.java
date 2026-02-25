package core.systems;

import core.states.SimulationContext;
import core.states.WorldState;
import core.commands.Command;
import core.commands.CommandType;
import core.states.effects.EffectCategory;
import core.states.effects.EffectData;
import core.states.events.EventType;
import core.states.events.GameEvent;

import java.util.Iterator;

public final class EffectSystem {

    public void update(SimulationContext context) {

        WorldState world = context.world();
        float dt = context.tick().deltaTime;

        Iterator<EffectData> iterator = world.effects().iterator();

        while (iterator.hasNext()) {

            EffectData effect = iterator.next();

            effect.elapsed += dt;

            if (effect.category == EffectCategory.GAMEPLAY || effect.category == EffectCategory.HYBRID) {
                processGameplay(effect, context);
            }

            if (effect.category == EffectCategory.VISUAL || effect.category == EffectCategory.HYBRID) {
                emitVisual(effect, context);
            }

            if (effect.elapsed >= effect.duration) {
                iterator.remove();
            }
        }
    }

    private void processGameplay(EffectData effect, SimulationContext context) {

        if (effect.tickInterval > 0f) {

            effect.tickAccumulator += context.tick().deltaTime;

            if (effect.tickAccumulator >= effect.tickInterval) {

                effect.tickAccumulator = 0f;

                Command command = new Command(CommandType.CUSTOM, effect.targetId);
                command.payload.putAll(effect.payload);
                context.commands().add(command);
            }
        }
    }

    private void emitVisual(EffectData effect, SimulationContext context) {

        GameEvent event = new GameEvent(EventType.CUSTOM);
        event.sourceId = effect.sourceId;
        event.payload.putAll(effect.payload);
        context.events().add(event);
    }
}
