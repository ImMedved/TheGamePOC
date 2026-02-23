package network.packets;

import network.Packet;
import network.PacketType;

public final class PlayerStatePacket implements Packet {

    public final int tick;
    public final int playerId;
    public final float x;
    public final float y;
    public final float velocityX;
    public final float velocityY;

    public PlayerStatePacket(
            int tick,
            int playerId,
            float x,
            float y,
            float velocityX,
            float velocityY
    ) {
        this.tick = tick;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    @Override
    public PacketType getType() {
        return PacketType.PLAYER_STATE;
    }
}
