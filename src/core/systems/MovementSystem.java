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
        System.out.println("[MOVE SYS] registry=" + registry.hashCode());
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

            System.out.println("[MOVE SYS] moveX=" + input.moveX + " moveY=" + input.moveY);
            //System.out.println("[MOVE SYS] is player.alive?:" +  player.alive);

            CharacterDefinition def = characterRegistry.get(Math.max(1, player.characterId));

            float speed = def.baseSpeed * player.speedMultiplier;
            System.out.println(
                    "[MOVE SYS] character=" + player.characterId +
                            " speed=" + speed +
                            " multiplier=" + player.speedMultiplier
            );
            float dx = context.input(player.id).moveX * speed * context.dt();
            float dy = context.input(player.id).moveY * speed * context.dt();

            float newX = player.position.x + dx;
            float newY = player.position.y + dy;
            System.out.println(
                    "[MOVE SYS] player=" + player.id +
                            " input=(" + input.moveX + "," + input.moveY + ")"
            );
            context.addCommand(new MovePlayerCommand(
                            player.id,
                            newX,
                            newY,
                            dx / context.dt(),
                            dy / context.dt()
                    )
            );
            System.out.println("[MOVE SYS] creating MovePlayerCommand for " + player.id);
        }
    }
}