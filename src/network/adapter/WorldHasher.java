package network.adapter;

import core.states.WorldState;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class WorldHasher {

    private final MessageDigest digest;

    public WorldHasher() {

        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] hash(WorldState world) {

        digest.reset();

        digest.update(intToBytes((int) world.tickIndex));

        world.players.values().forEach(player -> {

            digest.update(longToBytes(player.id));
            digest.update(intToBytes(player.characterId));

            digest.update(floatToBytes(player.position.x));
            digest.update(floatToBytes(player.position.y));

            digest.update(floatToBytes(player.health));
        });

        world.projectiles.forEach(p -> {

            digest.update(longToBytes(p.id));

            digest.update(floatToBytes(p.position.x));
            digest.update(floatToBytes(p.position.y));
        });

        return digest.digest();
    }

    private byte[] intToBytes(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }

    private byte[] longToBytes(long v) {
        return ByteBuffer.allocate(8).putLong(v).array();
    }

    private byte[] floatToBytes(float v) {
        return ByteBuffer.allocate(4).putFloat(v).array();
    }
}