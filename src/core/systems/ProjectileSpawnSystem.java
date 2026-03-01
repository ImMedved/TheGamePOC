package core.systems;

import core.SimulationContext;
import core.commands.SpawnProjectileCommand;
import core.registries.ProjectileDefinition;
import core.registries.ProjectileRegistry;
import core.states.PlayerState;

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

        if (!context.input().shoot)
            return;

        for (PlayerState player : context.snapshot().players.values()) {

            if (!player.alive) continue;

            int projectileType = 0;

            ProjectileDefinition def = projectileRegistry.get(projectileType);

            float dx = context.input().mouseX - player.position.x;
            float dy = context.input().mouseY - player.position.y;

            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len == 0f) return;

            float vx = (dx / len) * def.speed;
            float vy = (dy / len) * def.speed;

            // System.out.println("Spawn lifetime: " + def.lifetime);

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
        }
    }
}