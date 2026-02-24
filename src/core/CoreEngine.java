package core;

import input.InputModule;
import input.InputSnapshot;
import network.Packet;
import network.packets.HelloPacket;
import network.packets.PlayerStatePacket;
import network.packets.ProjectileSpawnPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

import core.workers.MovementWorker;
import core.workers.ProjectileWorker;
import core.workers.CollisionWorker;

public class CoreEngine implements WorldStateProvider{
    // world data, позже добавить чексумму при конекте
    // занижаем в нулину скорость цикла, потому что либо надо городить на сети сборщик данных из нескольких циклов в один пакет, а мне в падлу, либо пакеты будут пропадать.
    public static final int TICK_RATE = 20; // эээ, оно типо не работает, я хз, если добавить слип, то норм, а так не верю, что это 60/с
    // теперь тикрейт синхронится сразу в NetworkManager, по идее, они должны работать с одной частотой.

    private static final float TICK_DT = 1f / TICK_RATE; // пиздеж
    private static final float PLAYER_SPEED = 300f; // но надо фиксить, скорость же зависит от тикрейта, значит слипы не помогут
    private static final float PROJECTILE_SPEED = 500f;

    // game
    private final int localPlayerId;

    private final InputModule inputModule;

    private final AtomicReference<WorldState> activeWorld =
            new AtomicReference<>(WorldState.initial());

    private Thread coreThread;
    private volatile boolean running = false;

    int projectileIdCounter = 0;

    // сеть
    private final NetworkBridge network;
    private volatile boolean connected = false; // handshake
    private boolean helloSent = false;

    // Барьер и потоки воркеры
    private Thread movementThread;
    private Thread projectileThread;
    private Thread collisionThread;

    private CyclicBarrier tickBarrier;

    public volatile WorldState barrierSnapshot;
    public volatile InputSnapshot barrierInput;
    volatile int barrierTick;

    public volatile PlayerState barrierNewLocal;
    volatile List<ProjectileState> barrierUpdatedProjectiles;
    volatile CollisionResult barrierCollisionResult;

    private final List<BulletHoleState> bulletHoles = new ArrayList<>();

    public CoreEngine(InputModule inputModule, NetworkBridge network, int localPlayerId) {
        this.inputModule = inputModule;
        this.network = network;
        this.localPlayerId = localPlayerId;
    }

    @Override
    public WorldState getLatestWorldState() {
        return activeWorld.get();
    }

    public void start() {
        running = true;

        tickBarrier = new CyclicBarrier(4);

        MovementWorker movementWorker =
                new MovementWorker(this, tickBarrier);

        ProjectileWorker projectileWorker =
                new ProjectileWorker(this, tickBarrier);

        CollisionWorker collisionWorker =
                new CollisionWorker(this, tickBarrier);

        movementThread = new Thread(movementWorker);
        movementThread.setName("MovementThread");
        movementThread.start();

        projectileThread = new Thread(projectileWorker);
        projectileThread.setName("ProjectileThread");
        projectileThread.start();

        Thread collisionThread = new Thread(collisionWorker);
        collisionThread.setName("CollisionThread");
        collisionThread.start();

        coreThread = new Thread(this::runLoop);
        coreThread.setName("CoreThread");
        coreThread.start();
    }

    public void stop() {
        running = false;

        try {
            if (coreThread != null) coreThread.join();
            if (movementThread != null) movementThread.join();
            if (projectileThread != null) projectileThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    private void runLoop() {

        int tick = 0;

        while (running) {

            long startTime = System.currentTimeMillis();

            runTick(tick);

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = (1000 / TICK_RATE) - elapsed;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
            tick++;
        }
    }

    private void runTick(int tick) {

        WorldState active = activeWorld.get();

        PlayerState currentRemote = active.remotePlayer;

        List<ProjectileState> incomingProjectiles = new ArrayList<>();

        // обработка входящих сетевых пакетов
        Packet netPacket;

        while ((netPacket = network.poll()) != null) {

            switch (netPacket.getType()) {

                case PLAYER_STATE -> {

                    PlayerStatePacket p = (PlayerStatePacket) netPacket;

                    if (p.playerId != localPlayerId) {

                        currentRemote = new PlayerState(
                                p.playerId,
                                p.x,
                                p.y,
                                p.velocityX,
                                p.velocityY,
                                currentRemote.hitboxRadius
                        );
                    }
                    System.out.println("Received PLAYER_STATE on local: " + localPlayerId);
                }

                case PROJECTILE_SPAWN -> {
                    ProjectileSpawnPacket p = (ProjectileSpawnPacket) netPacket;

                    if (p.ownerId != localPlayerId) {

                        incomingProjectiles.add(
                                new ProjectileState(
                                        p.projectileId,
                                        p.ownerId,
                                        p.startX,
                                        p.startY,
                                        p.dirX,
                                        p.dirY,
                                        PROJECTILE_SPEED,
                                        p.startX,
                                        p.startY
                                )
                        );
                    }
                }

                case HELLO -> {
                    connected = true;
                    // это временная затычка до того, как будет добавлен судья, так как сейчас мы не подтверждаем при конекте кто есть кто
                    System.out.println("[NET] HELLO received from player");
                }
            }
        }

        // локальный input
        inputModule.publishSnapshot(tick);
        InputSnapshot localInput = inputModule.getLatestSnapshot();

        final WorldState snapshot = active;

        // барьерная синхронизация

        barrierSnapshot = active;
        barrierInput = localInput;
        barrierTick = tick;

        try {
            tickBarrier.await(); // старт тик-фазы
            tickBarrier.await(); // ожидание завершения воркеров
        } catch (InterruptedException | BrokenBarrierException ignored) {
        }

        PlayerState newLocal = barrierNewLocal;
        List<ProjectileState> updatedProjectiles = barrierUpdatedProjectiles;

        CollisionResult collision = barrierCollisionResult;

        // сборщик даты в объект staging world state
        WorldState newWorld = new WorldState(
                tick + 1,
                newLocal,
                currentRemote,
                collision.projectiles,
                collision.gameOver,
                collision.winnerId,
                active.getWorldWidth(),
                active.getWorldHeight()
        );

        activeWorld.set(newWorld);

        if (!helloSent) {
            network.send(new HelloPacket(localPlayerId));
            System.out.println("[NET] Sending HELLO, local ID is: " + localPlayerId);
            helloSent = true;
        }

        if (tick != -1){
            float velocityX = localInput.moveX * PLAYER_SPEED;
            float velocityY = localInput.moveY * PLAYER_SPEED;

            PlayerStatePacket packet =
                    new PlayerStatePacket(
                            tick,
                            localPlayerId,
                            newLocal.x,
                            newLocal.y,
                            velocityX,
                            velocityY
                    );

            network.send(packet);

        }else{
            System.out.println("Not connected, nothing sent");
        }
    }

    // --- barrier getters ---

    public WorldState getBarrierSnapshot() {
        return barrierSnapshot;
    }

    public InputSnapshot getBarrierInput() {
        return barrierInput;
    }

    public int getBarrierTick() {
        return barrierTick;
    }

// --- barrier setters ---

    void setBarrierNewLocal(PlayerState state) {
        this.barrierNewLocal = state;
    }

    public void setBarrierUpdatedProjectiles(List<ProjectileState> list) {
        this.barrierUpdatedProjectiles = list;
    }

    public void setBarrierCollisionResult(CollisionResult result) {
        this.barrierCollisionResult = result;
    }

    public int nextProjectileId() {
        return projectileIdCounter++;
    }

    public PlayerState getBarrierNewLocal() {
        return barrierNewLocal;
    }

    public List<ProjectileState> getBarrierUpdatedProjectiles() {
        return barrierUpdatedProjectiles;
    }
}