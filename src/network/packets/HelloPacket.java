package network.packets;

// отключено
public final class HelloPacket implements Packet {

    public final int playerId;

    public HelloPacket(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public PacketType getType() {
        return PacketType.HELLO;
    }
}
