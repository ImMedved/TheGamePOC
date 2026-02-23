package network;

public enum PacketType {

    HELLO(0),
    PLAYER_STATE(1),
    PROJECTILE_SPAWN(2);

    public final byte id;

    PacketType(int id) {
        this.id = (byte) id;
    }

    public static PacketType fromId(byte id) {
        for (PacketType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown packet id: " + id);
    }
}
