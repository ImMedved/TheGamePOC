package core.workers;

import core.*;
import core.states.PlayerState;
import core.states.ProjectileState;
import input.InputSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ProjectileWorker implements Runnable {

    private final CoreEngine engine;
    private final CyclicBarrier barrier;

    private static final float PROJECTILE_SPEED = 500f;
    private static final float TICK_DT =
            1f / CoreEngine.TICK_RATE;

    public ProjectileWorker(CoreEngine engine,
                            CyclicBarrier barrier) {
        this.engine = engine;
        this.barrier = barrier;
    }

    @Override
    public void run() {

        while (true) {

            try {
                barrier.await();

                engine.setBarrierUpdatedProjectiles(
                        updateProjectiles(
                                engine.getBarrierSnapshot().projectiles,
                                engine.getBarrierNewLocal(),
                                engine.getBarrierInput(),
                                engine.getBarrierTick(),
                                engine.getBarrierSnapshot().getWorldWidth(),
                                engine.getBarrierSnapshot().getWorldHeight()
                        )
                );

                barrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                break;
            }
        }
    }

    private List<ProjectileState> updateProjectiles(
            List<ProjectileState> projectiles,
            PlayerState newLocal,
            InputSnapshot input,
            int tick,
            float worldWidth,
            float worldHeight) {

        List<ProjectileState> result =
                new ArrayList<>();

        if (input.shoot) {

            float dx =
                    input.mouseX - newLocal.x;
            float dy =
                    input.mouseY - newLocal.y;

            float length =
                    (float)Math.sqrt(dx*dx + dy*dy);

            if (length > 0) {

                float dirX = dx / length;
                float dirY = dy / length;

                result.add(new ProjectileState(
                        engine.nextProjectileId(),
                        newLocal.playerId,
                        newLocal.x,
                        newLocal.y,
                        dirX,
                        dirY,
                        PROJECTILE_SPEED,
                        newLocal.x,
                        newLocal.y
                ));
            }
        }

        for (ProjectileState p : projectiles) {

            float newX =
                    p.x + p.dirX * p.speed * TICK_DT;
            float newY =
                    p.y + p.dirY * p.speed * TICK_DT;

            float dx = newX - p.startX;
            float dy = newY - p.startY;

            float dist = (float)Math.sqrt(dx*dx + dy*dy);

            if (dist > 800f) {
                continue;
            }

            if (newX >= 0 &&
                    newX <= worldWidth &&
                    newY >= 0 &&
                    newY <= worldHeight) {

                result.add(new ProjectileState(
                        p.projectileId,
                        p.ownerId,
                        newX,
                        newY,
                        p.dirX,
                        p.dirY,
                        p.speed,
                        p.startX,
                        p.startY
                ));
            }
        }

        return result;
    }
}