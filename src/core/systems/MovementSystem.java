package core.systems;

import core.SimulationContext;
import core.commands.MovePlayerCommand;
import core.registries.CharacterDefinition;
import core.registries.CharacterRegistry;
import core.states.PlayerState;
import input.InputSnapshot;

import java.util.Comparator;

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

        for (PlayerState player : context.snapshot().players.values()
                .stream()
                .sorted(Comparator.comparingLong(p -> p.id))
                .toList()) {
            InputSnapshot input = context.input(player.id);

            if (input == null)
                continue;

            if (!player.alive) continue;

            CharacterDefinition def = characterRegistry.get(player.characterId);

            float speed = def.baseSpeed * player.speedMultiplier;
            // System.out.println("speedMultiplier in MovementSystem: " + player.speedMultiplier);

            float dx = context.input(player.id).moveX * speed * context.dt();
            float dy = context.input(player.id).moveY * speed * context.dt();

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