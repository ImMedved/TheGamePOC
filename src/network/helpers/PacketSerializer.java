package network.helpers;

import network.packets.HelloPacket;
import network.packets.Packet;
import network.packets.PlayerStatePacket;
import network.packets.ProjectileSpawnPacket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketSerializer {

    public static byte[] serialize(Packet packet) {

        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(packet.getType().id);

        switch (packet.getType()) {

            case HELLO -> {
                HelloPacket p = (HelloPacket) packet;
                buffer.putInt(p.playerId);
            }

            case PLAYER_STATE -> {
                PlayerStatePacket p = (PlayerStatePacket) packet;
                buffer.putInt(p.tick);
                buffer.putInt(p.playerId);
                buffer.putFloat(p.x);
                buffer.putFloat(p.y);
                buffer.putFloat(p.velocityX);
                buffer.putFloat(p.velocityY);
            }

            case PROJECTILE_SPAWN -> {
                ProjectileSpawnPacket p = (ProjectileSpawnPacket) packet;
                buffer.putInt(p.tick);
                buffer.putInt(p.projectileId);
                buffer.putInt(p.ownerId);
                buffer.putFloat(p.startX);
                buffer.putFloat(p.startY);
                buffer.putFloat(p.dirX);
                buffer.putFloat(p.dirY);
            }
        }

        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);

        return data;
    }
}
