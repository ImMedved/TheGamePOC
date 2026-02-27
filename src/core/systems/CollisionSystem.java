package core.systems;

import core.SimulationContext;
import core.commands.DamagePlayerCommand;
import core.commands.RemoveProjectileCommand;
import core.states.PlayerState;
import core.states.ProjectileState;

public final class CollisionSystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.SEQUENTIAL;
    }

    @Override
    public void update(SimulationContext context) {

        for (ProjectileState projectile : context.snapshot().projectiles) {

            for (PlayerState player : context.snapshot().players.values()) {

                if (!player.alive) continue;
                if (projectile.ownerId == player.id) continue;

                float dx = projectile.position.x - player.position.x;

                float dy = projectile.position.y - player.position.y;

                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist <= player.hitboxRadius) {
                    context.addCommand(new DamagePlayerCommand(player.id, projectile.damage));
                    context.addCommand(new RemoveProjectileCommand(projectile.id));
                }
            }
        }
    }
}