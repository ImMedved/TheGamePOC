package core.systems;

import core.states.SimulationContext;
import core.commands.DamagePlayerCommand;
import core.commands.RemoveProjectileCommand;
import core.states.ProjectileState;

public final class CollisionSystem {

    public void update(SimulationContext context) {

        for (ProjectileState p : context.snapshot().projectiles()) {

            float dx = p.position.x - context.snapshot().player().position.x;
            float dy = p.position.y - context.snapshot().player().position.y;

            float dist = (float)Math.sqrt(dx*dx + dy*dy);

            if (dist <= 20f) {

                context.commands().add(
                        new DamagePlayerCommand(10f)
                );

                context.commands().add(
                        new RemoveProjectileCommand(p.id)
                );
            }
        }
    }
}