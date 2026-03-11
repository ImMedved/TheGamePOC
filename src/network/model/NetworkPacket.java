package network.model;

public final class NetworkPacket {

    public enum PacketType {
        INPUT,
        STATE_HASH,

        GAME_START,
        VDF_RESULT,
        MOVE,
        VALIDATION,
        VOID
    }

    private final NodeId sender;
    private final int sequenceNumber;
    private final int tickNumber;

    private final PacketType type;

    private final byte[] payload;
    private final byte[] signature;

    public NetworkPacket(
            NodeId sender,
            int sequenceNumber,
            int tickNumber,
            PacketType type,
            byte[] payload,
            byte[] signature
    ) {
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
        this.tickNumber = tickNumber;
        this.type = type;
        this.payload = payload;
        this.signature = signature;
    }

    public NodeId sender() {
        return sender;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    public int tickNumber() {
        return tickNumber;
    }

    public PacketType type() {
        return type;
    }

    public byte[] payload() {
        return payload;
    }

    public byte[] signature() {
        return signature;
    }
}