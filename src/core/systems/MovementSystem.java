package core.systems;

import core.states.SimulationContext;
import core.commands.MovePlayerCommand;
import core.states.PlayerState;

public final class MovementSystem {

    private static final float PLAYER_SPEED = 300f;

    public void update(SimulationContext context) {

        PlayerState player = context.snapshot().player();

        float dx = context.input().moveX * PLAYER_SPEED * context.dt();
        float dy = context.input().moveY * PLAYER_SPEED * context.dt();

        float newX = player.position.x + dx;
        float newY = player.position.y + dy;

        context.commands().add(
                new MovePlayerCommand(
                        newX,
                        newY,
                        dx / context.dt(),
                        dy / context.dt()
                )
        );
    }
}