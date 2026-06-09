package network.validation;

import core.states.PlayerState;
import core.states.ProjectileState;
import core.states.WorldState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StateFramePayload {

    public final int tick;
    public final List<PlayerFrame> players;
    public final List<ProjectileFrame> projectiles;
    public final boolean gameOver;
    public final long winnerPlayerId;

    public StateFramePayload(
            int tick,
            List<PlayerFrame> players,
            List<ProjectileFrame> projectiles,
            boolean gameOver,
            long winnerPlayerId
    ) {
        this.tick = tick;
        this.players = players;
        this.projectiles = projectiles;
        this.gameOver = gameOver;
        this.winnerPlayerId = winnerPlayerId;
    }

    public static StateFramePayload fromWorld(WorldState world) {
        List<PlayerFrame> players = new ArrayList<>();
        world.players.values().stream()
                .sorted(Comparator.comparingLong(p -> p.id))
                .forEach(p -> players.add(new PlayerFrame(
                        p.id,
                        p.position.x,
                        p.position.y,
                        p.health,
                        p.alive,
                        p.livesRemaining
                )));

        List<ProjectileFrame> projectiles = new ArrayList<>();
        world.projectiles.stream()
                .sorted(Comparator.comparingLong(p -> p.id))
                .forEach(p -> projectiles.add(new ProjectileFrame(
                        p.id,
                        p.ownerId,
                        p.position.x,
                        p.position.y,
                        p.velocity.x,
                        p.velocity.y
                )));

        return new StateFramePayload(
                Math.toIntExact(world.tickIndex),
                players,
                projectiles,
                world.gameOver,
                world.winnerPlayerId
        );
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);

            out.writeInt(tick);
            out.writeBoolean(gameOver);
            out.writeLong(winnerPlayerId);

            out.writeInt(players.size());
            for (PlayerFrame player : players) {
                out.writeLong(player.id);
                out.writeFloat(player.x);
                out.writeFloat(player.y);
                out.writeFloat(player.health);
                out.writeBoolean(player.alive);
                out.writeInt(player.livesRemaining);
            }

            out.writeInt(projectiles.size());
            for (ProjectileFrame projectile : projectiles) {
                out.writeLong(projectile.id);
                out.writeLong(projectile.ownerId);
                out.writeFloat(projectile.x);
                out.writeFloat(projectile.y);
                out.writeFloat(projectile.velocityX);
                out.writeFloat(projectile.velocityY);
            }

            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode state frame", e);
        }
    }

    public static StateFramePayload fromBytes(byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            int tick = in.readInt();
            boolean gameOver = in.readBoolean();
            long winnerPlayerId = in.readLong();

            int playerCount = in.readInt();
            List<PlayerFrame> players = new ArrayList<>(playerCount);
            for (int i = 0; i < playerCount; i++) {
                players.add(new PlayerFrame(
                        in.readLong(),
                        in.readFloat(),
                        in.readFloat(),
                        in.readFloat(),
                        in.readBoolean(),
                        in.readInt()
                ));
            }

            int projectileCount = in.readInt();
            List<ProjectileFrame> projectiles = new ArrayList<>(projectileCount);
            for (int i = 0; i < projectileCount; i++) {
                projectiles.add(new ProjectileFrame(
                        in.readLong(),
                        in.readLong(),
                        in.readFloat(),
                        in.readFloat(),
                        in.readFloat(),
                        in.readFloat()
                ));
            }

            return new StateFramePayload(tick, players, projectiles, gameOver, winnerPlayerId);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to decode state frame", e);
        }
    }

    public record PlayerFrame(long id, float x, float y, float health, boolean alive, int livesRemaining) {
    }

    public record ProjectileFrame(long id, long ownerId, float x, float y, float velocityX, float velocityY) {
    }
}
