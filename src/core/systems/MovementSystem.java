package core.systems;

import core.SimulationContext;
import core.commands.MovePlayerCommand;
import core.registries.CharacterDefinition;
import core.registries.CharacterRegistry;
import core.states.PlayerState;

public final class MovementSystem implements GameSystem {

    private final CharacterRegistry characterRegistry;

    public MovementSystem(CharacterRegistry registry) {
        this.characterRegistry = registry;
    }

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        for (PlayerState player : context.snapshot().players.values()) {

            if (!player.alive) continue;

            CharacterDefinition def = characterRegistry.get(player.characterId);

            float dx = context.input().moveX * def.baseSpeed * context.dt();

            float dy = context.input().moveY * def.baseSpeed * context.dt();

            float newX = player.position.x + dx;
            float newY = player.position.y + dy;

            context.addCommand(new MovePlayerCommand(
                            player.id,
                            newX,
                            newY,
                            dx / context.dt(),
                            dy / context.dt()
                    )
            );
        }
    }
}