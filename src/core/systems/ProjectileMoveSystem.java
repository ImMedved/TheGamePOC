package core.systems;

import core.SimulationContext;
import core.commands.ApplyEffectCommand;
import core.commands.MoveProjectileCommand;
import core.commands.RemoveProjectileCommand;
import core.config.EffectConfigs;
import core.config.ProjectileEffectConfigs;
import core.factories.EffectFactory;
import core.states.ProjectileState;

public final class ProjectileMoveSystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        float dt = context.dt();

        for (ProjectileState p : context.snapshot().projectiles) {

            float newX = p.position.x + p.velocity.x * dt;
            float newY = p.position.y + p.velocity.y * dt;
            float newElapsed = p.elapsed + dt;

            float dx = p.velocity.x * dt;
            float dy = p.velocity.y * dt;

            float frameDistance = (float)Math.sqrt(dx * dx + dy * dy);
            float newDistance = p.traveledDistance + frameDistance;

            context.addCommand(
                    new MoveProjectileCommand(
                            p.id,
                            newX,
                            newY,
                            p.velocity.x,
                            p.velocity.y,
                            newElapsed,
                            newDistance
                    )
            );

            // System.out.println("p.maxDistance is: " + p.maxDistance + " newDistance is: " + newDistance);
            if (p.maxDistance > 0f && newDistance >= p.maxDistance) {

                context.addCommand(
                        new ApplyEffectCommand(
                                EffectFactory.createBulletHole(
                                        context.nextId(),
                                        p.id,
                                        EffectConfigs.BULLET_HOLE,
                                        newX,
                                        newY
                                )
                        )
                );

                context.addCommand(new RemoveProjectileCommand(p.id));
            }
            //System.out.println("Elapsed: " + p.elapsed);
        }
    }
}