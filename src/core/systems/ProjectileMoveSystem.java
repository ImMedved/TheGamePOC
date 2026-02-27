package core.systems;

import core.SimulationContext;
import core.commands.MoveProjectileCommand;
import core.commands.RemoveProjectileCommand;
import core.states.ProjectileState;

public final class ProjectileMoveSystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        for (ProjectileState p : context.snapshot().projectiles) {

            float newX = p.position.x + p.velocity.x * context.dt();
            float newY = p.position.y + p.velocity.y * context.dt();

            context.addCommand(new MoveProjectileCommand(
                            p.id,
                            newX,
                            newY,
                            p.velocity.x,
                            p.velocity.y
                    )
            );

            if (p.elapsed + context.dt() >= p.lifetime) {
                context.addCommand(new RemoveProjectileCommand(p.id));
            }
        }
    }
}