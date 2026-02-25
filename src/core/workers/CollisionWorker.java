package core.workers;

import core.*;
import core.states.PlayerState;
import core.states.ProjectileState;
import core.workers.helpers.CollisionResult;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CollisionWorker implements Runnable {

    private final CoreEngine engine;
    private final CyclicBarrier barrier;

    public CollisionWorker(CoreEngine engine,
                           CyclicBarrier barrier) {
        this.engine = engine;
        this.barrier = barrier;
    }

    @Override
    public void run() {

        while (true) {

            try {
                barrier.await();

                engine.setBarrierCollisionResult(
                        detectCollisions(
                                engine.getBarrierUpdatedProjectiles(),
                                engine.getBarrierNewLocal(),
                                engine.getBarrierSnapshot().remotePlayer
                        )
                );

                barrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                break;
            }
        }
    }

    private CollisionResult detectCollisions(
            List<ProjectileState> projectiles,
            PlayerState local,
            PlayerState remote) {

        for (ProjectileState p : projectiles) {

            PlayerState target =
                    (p.ownerId == local.playerId)
                            ? remote
                            : local;

            float dx = p.x - target.x;
            float dy = p.y - target.y;

            float dist =
                    (float)Math.sqrt(dx*dx + dy*dy);

            if (dist <= target.hitboxRadius) {

                return new CollisionResult(
                        List.of(),
                        true,
                        p.ownerId
                );
            }
        }

        return new CollisionResult(
                projectiles,
                false,
                -1
        );
    }
}