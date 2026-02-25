package core.systems;

import core.states.SimulationContext;
import core.commands.SpawnProjectileCommand;
import core.states.PlayerState;

public final class ProjectileSystem {

    private static final float PROJECTILE_SPEED = 500f;

    public void update(SimulationContext context) {

        if (!context.input().shoot)
            return;

        PlayerState player = context.snapshot().player();

        float dx = context.input().mouseX - player.position.x;
        float dy = context.input().mouseY - player.position.y;

        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len == 0f) return;

        float vx = (dx / len) * PROJECTILE_SPEED;
        float vy = (dy / len) * PROJECTILE_SPEED;

        context.commands().add(
                new SpawnProjectileCommand(
                        context.nextId(),
                        player.position.x,
                        player.position.y,
                        vx,
                        vy
                )
        );
    }
}