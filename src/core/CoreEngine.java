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

public class CoreEngine implements WorldStateProvider{
    // world data, позже добавить чексумму при конекте
    // занижаем в нулину скорость цикла, потому что либо надо городить на сети сборщик данных из нескольких циклов в один пакет, а мне в падлу, либо пакеты будут пропадать.
    public static final int TICK_RATE = 10; // эээ, оно типо не работает, я хз, если добавить слип, то норм, а так не верю, что это 60/с
    // теперь тикрейт синхронится сразу в NetworkManager, по идее, они должны работать с одной частотой.

    private static final float TICK_DT = 1f / TICK_RATE; // пиздеж
    private static final float PLAYER_SPEED = 300f; // но надо фиксить, скорость же зависит от тикрейта, значит слипы не помогут
    private static final float PROJECTILE_SPEED = 500f;

    //private static final float ARENA_WIDTH = 800f; // подставить размеры окна из main??? -> подставил из WorldState
    //private static final float ARENA_HEIGHT = 600f;

    // game
    private final int localPlayerId;

    private final InputModule inputModule;

    private final AtomicReference<WorldState> activeWorld =
            new AtomicReference<>(WorldState.initial());

    private Thread coreThread;
    private volatile boolean running = false;

    private int projectileIdCounter = 0;

    // сеть
    private final NetworkBridge network;
    private volatile boolean connected = false; // handshake
    private boolean helloSent = false;

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
        coreThread = new Thread(this::runLoop);
        coreThread.setName("CoreThread");
        coreThread.start();
    }

    public void stop() {
        running = false;
        if (coreThread != null) {
            try {
                coreThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void runLoop() {

        int tick = 0;

        while (running) {

            long startTime = System.currentTimeMillis();

            runTick(tick);

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = (1000 / TICK_RATE) - elapsed;

            // вот тут вопрос, потому что я не очень выкупаю, сколько оно на самом деле спит. Типо по идее должно быть 16 мс, но на деле
            // типо я не верю. Надо балансить, с одной стороны, с другой, если делать сеть, то может быть ботлнек, из-за кол-ва пакетов.
            // мб просто поставить сон на секунду
            // Upd: понял
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
            tick++;
        }
    }

    /**
     * Эта залупа ежа берет данные из прошлого состояния мира, затем мутирует их через вводы и созраняет в stagging.
     * После делается свап в active. Лол, я сюда впихнул слип, когда у меня родительском методе компенсация.
     * @param tick
     */
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
                                        PROJECTILE_SPEED
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

        /** дивжение локала однопоточка
        PlayerState newLocal =
                computeLocalMovement(
                        active.localPlayer,
                        localInput,
                        active.getWorldWidth(),
                        active.getWorldHeight()
                );

        List<ProjectileState> updatedProjectiles =
                updateProjectiles(
                        active.projectiles,
                        newLocal,
                        localInput,
                        tick,
                        active.getWorldWidth(),
                        active.getWorldHeight()
                );

         */

        final WorldState snapshot = active;

        final PlayerState[] localHolder = new PlayerState[1];
        final List<ProjectileState>[] projectileHolder = new List[1];

        Thread movementThread = new Thread(() -> {
            localHolder[0] = computeLocalMovement(
                    snapshot.localPlayer,
                    localInput,
                    snapshot.getWorldWidth(),
                    snapshot.getWorldHeight()
            );
        });

        Thread projectileThread = new Thread(() -> {
            projectileHolder[0] = updateProjectiles(
                    snapshot.projectiles,
                    snapshot.localPlayer,
                    localInput,
                    tick,
                    snapshot.getWorldWidth(),
                    snapshot.getWorldHeight()
            );
        });

        movementThread.start();
        projectileThread.start();

        try {
            movementThread.join();
            projectileThread.join();
        } catch (InterruptedException ignored) {
        }

        PlayerState newLocal = localHolder[0];
        List<ProjectileState> updatedProjectiles = projectileHolder[0];

        // Сначала сохранить проджектайлы в отдельный лист, потом замержить, чтобы не рушить порядок объявления
        // сохраняем имутабильную модель, если на умном
        updatedProjectiles.addAll(incomingProjectiles);

        CollisionResult collision =
                detectCollisions(updatedProjectiles, newLocal, currentRemote);

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

        // свап (который не свап, но похуй, смысл был изначально другой)
        activeWorld.set(newWorld);

        // сеть
        /**
         * Кароч, смысл такой, что мы в начале обмениваемся пакетами состояния, которые поставят игроков на позиции и
         * возщможно позже будут проверять состояние синхронизации. Но сейчас нужно переключить сеть на обмен данными,
         * так как в начале helloSent стоит на обмене handshake. Если HELLO не был выбран как case получаемого пакета,
         * то сеть не перейдет в connected == true. Upd: см фикс ниже
         */
        if (!helloSent) {
            network.send(new HelloPacket(localPlayerId));
            System.out.println("[NET] Sending HELLO, local ID is: " + localPlayerId);
            helloSent = true;
        }

        /** квик фикс, убрать зависимость от connected, пока не будет логики цикла проверки соединения,
         * иначе на втором пк не приходит hello TODO
         */

        //if (tick % 4 == 0) {
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

            /**
             * Проблема в том, что сеть не может работать с 30+30 пакетами в две стороны в секунду
             * Так что нужно либо объединять их в пакеты пакетов, что типо бессмыстленно
             * Либо нужно понижать тикрейт всей игры, чтобы мы отправляляи, например, 10+10 пакетов.
             */
            /* System.out.println("Tick " + tick +
                " | snapshot data: " + localInput);
            System.out.println("Tick " + tick +
                " | Local(" + newLocal.x + "," + newLocal.y + ")" +
                " | Projectiles: " + collision.projectiles.size());*/

        }else{
            System.out.println("Not connected, nothing sent");
        }
    }

    private PlayerState computeLocalMovement(PlayerState player,
                                             InputSnapshot input,
                                             float worldWidth,
                                             float worldHeight) {

        // System.out.println("Input snapshot: " + input.toString());
        float deltaX = input.moveX * PLAYER_SPEED * TICK_DT;
        float deltaY = input.moveY * PLAYER_SPEED * TICK_DT;

        float newX = clamp(player.x + deltaX, 0, worldWidth);
        float newY = clamp(player.y + deltaY, 0, worldHeight);

        return new PlayerState(
                player.playerId,
                newX,
                newY,
                deltaX / TICK_DT,
                deltaY / TICK_DT,
                player.hitboxRadius
        );
    }

    private List<ProjectileState> updateProjectiles(
            List<ProjectileState> projectiles,
            PlayerState newLocal,
            InputSnapshot input,
            int tick,
            float worldWidth,
            float worldHeight
    ) {

        List<ProjectileState> result = new ArrayList<>();

        if (input.shoot) {

            float dx = input.mouseX - newLocal.x;
            float dy = input.mouseY - newLocal.y;

            float length = (float) Math.sqrt(dx * dx + dy * dy);

            if (length > 0) {

                float dirX = dx / length;
                float dirY = dy / length;

                result.add(new ProjectileState(
                        projectileIdCounter++,
                        newLocal.playerId,
                        newLocal.x,
                        newLocal.y,
                        dirX,
                        dirY,
                        PROJECTILE_SPEED
                ));
                network.send(new ProjectileSpawnPacket(
                        tick,
                        projectileIdCounter - 1 + 10000, // по идее тогда они не должны наслаиваться с равными идами.
                        newLocal.playerId,
                        newLocal.x,
                        newLocal.y,
                        dirX,
                        dirY
                ));
            }
        }

        for (ProjectileState p : projectiles) {

            float newX = p.x + p.dirX * p.speed * TICK_DT;
            float newY = p.y + p.dirY * p.speed * TICK_DT;

            if (newX >= 0 && newX <= worldWidth &&
                    newY >= 0 && newY <= worldHeight) {

                result.add(new ProjectileState(
                        p.projectileId,
                        p.ownerId,
                        newX,
                        newY,
                        p.dirX,
                        p.dirY,
                        p.speed
                ));
            }
        }

        return result;
    }

    private CollisionResult detectCollisions(
            List<ProjectileState> projectiles,
            PlayerState local,
            PlayerState remote
    ) {

        for (ProjectileState p : projectiles) {

            PlayerState target = (p.ownerId == local.playerId) ? remote : local;

            float dx = p.x - target.x;
            float dy = p.y - target.y;

            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist <= target.hitboxRadius) {

                return new CollisionResult(
                        List.of(),
                        true,
                        p.ownerId
                );
            }
        }

        return new CollisionResult(projectiles, false, -1);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
