package network.packets;

import network.Packet;
import network.PacketType;

public final class ProjectileSpawnPacket implements Packet {

    public final int tick;
    public final int projectileId;
    public final int ownerId;
    public final float startX;
    public final float startY;
    public final float dirX;
    public final float dirY;

    public ProjectileSpawnPacket(
            int tick,
            int projectileId,
            int ownerId,
            float startX,
            float startY,
            float dirX,
            float dirY
    ) {
        this.tick = tick;
        this.projectileId = projectileId;
        this.ownerId = ownerId;
        this.startX = startX;
        this.startY = startY;
        this.dirX = dirX;
        this.dirY = dirY;
    }

    @Override
    public PacketType getType() {
        return PacketType.PROJECTILE_SPAWN;
    }
}

