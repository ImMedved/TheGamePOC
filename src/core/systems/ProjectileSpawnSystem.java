package core.systems;

import core.SimulationContext;
import core.commands.SpawnProjectileCommand;
import core.registries.ProjectileDefinition;
import core.registries.ProjectileRegistry;
import core.states.PlayerState;
import input.InputSnapshot;

import java.util.Comparator;

public final class ProjectileSpawnSystem implements GameSystem {

    private final ProjectileRegistry projectileRegistry;

    public ProjectileSpawnSystem(ProjectileRegistry registry) {
        this.projectileRegistry = registry;
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
            if (input == null) continue;

            if (!input.shoot) continue;

            if (player.id != input.ownerId) continue;

            if (player.shootCooldownRemaining > 0f) continue;

            if (!player.alive) continue;

            int projectileType = 0;

            ProjectileDefinition def = projectileRegistry.get(projectileType);

            float worldMouseX = input.mouseX;
            float worldMouseY = input.mouseY;

            float dx = worldMouseX - player.position.x;
            float dy = worldMouseY - player.position.y;

            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len == 0f) continue;

            float vx = (dx / len) * def.speed;
            float vy = (dy / len) * def.speed;


            context.addCommand(new SpawnProjectileCommand(
                            context.nextId(),
                            player.id,
                            projectileType,
                            player.position.x,
                            player.position.y,
                            vx,
                            vy
                    )
            );
            player.shootCooldownRemaining = PlayerState.SHOOT_COOLDOWN;
        }
    }
}
