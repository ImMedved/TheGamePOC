package core.workers;

import core.*;
import input.InputSnapshot;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MovementWorker implements Runnable {

    private final CoreEngine engine;
    private final CyclicBarrier barrier;

    private static final float PLAYER_SPEED = 300f;
    private static final float TICK_DT =
            1f / CoreEngine.TICK_RATE;

    public MovementWorker(CoreEngine engine,
                          CyclicBarrier barrier) {
        this.engine = engine;
        this.barrier = barrier;
    }

    @Override
    public void run() {

        while (true) {

            try {
                barrier.await();

                engine.barrierNewLocal =
                        computeLocalMovement(
                                engine.barrierSnapshot.localPlayer,
                                engine.barrierInput,
                                engine.barrierSnapshot.getWorldWidth(),
                                engine.barrierSnapshot.getWorldHeight()
                        );

                barrier.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                break;
            }
        }
    }

    private PlayerState computeLocalMovement(PlayerState player,
                                             InputSnapshot input,
                                             float worldWidth,
                                             float worldHeight) {

        float deltaX =
                input.moveX * PLAYER_SPEED * TICK_DT;
        float deltaY =
                input.moveY * PLAYER_SPEED * TICK_DT;

        float wall = 100f;

        float minX = wall + player.hitboxRadius;
        float minY = wall + player.hitboxRadius;

        float maxX =
                worldWidth - wall - player.hitboxRadius;
        float maxY =
                worldHeight - wall - player.hitboxRadius;

        float newX = clamp(player.x + deltaX, minX, maxX);
        float newY = clamp(player.y + deltaY, minY, maxY);

        return new PlayerState(
                player.playerId,
                newX,
                newY,
                deltaX / TICK_DT,
                deltaY / TICK_DT,
                player.hitboxRadius
        );
    }

    private float clamp(float v,
                        float min,
                        float max) {
        return Math.max(min, Math.min(max, v));
    }
}