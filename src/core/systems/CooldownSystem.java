package core.systems;

import core.SimulationContext;
import core.states.PlayerState;
import input.InputSnapshot;

public final class CooldownSystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        for (PlayerState p : context.snapshot().players.values()) {
                InputSnapshot input = context.input(p.id);

            float dt = context.dt();

            p.shootCooldownRemaining =
                    Math.max(0f, p.shootCooldownRemaining - dt);

            p.tripleShotCooldownRemaining =
                    Math.max(0f, p.tripleShotCooldownRemaining - dt);

            p.speedCooldownRemaining =
                    Math.max(0f, p.speedCooldownRemaining - dt);

            p.blinkCooldownRemaining =
                    Math.max(0f, p.blinkCooldownRemaining - dt);
        }
    }
}