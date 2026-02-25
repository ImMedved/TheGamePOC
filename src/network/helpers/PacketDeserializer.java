package network.helpers;

import network.packets.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketDeserializer {

    public static Packet deserialize(byte[] data) {

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        PacketType type = PacketType.fromId(buffer.get());

        return switch (type) {

            case HELLO -> new HelloPacket(
                    buffer.getInt()
            );

            case PLAYER_STATE -> new PlayerStatePacket(
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat()
            );

            case PROJECTILE_SPAWN -> new ProjectileSpawnPacket(
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat()
            );
        };
    }
}
