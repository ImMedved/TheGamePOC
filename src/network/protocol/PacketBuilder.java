package network.protocol;

import network.model.NetworkPacket;
import network.model.NodeId;

public final class PacketBuilder {

    private NodeId sender;
    private int sequence;
    private int tick;
    private NetworkPacket.PacketType type;
    private byte[] payload;
    private byte[] signature;

    public PacketBuilder sender(NodeId sender) {
        this.sender = sender;
        return this;
    }

    public PacketBuilder sequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public PacketBuilder tick(int tick) {
        this.tick = tick;
        return this;
    }

    public PacketBuilder type(NetworkPacket.PacketType type) {
        this.type = type;
        return this;
    }

    public PacketBuilder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public PacketBuilder signature(byte[] signature) {
        this.signature = signature;
        return this;
    }

    public NetworkPacket build() {

        return new NetworkPacket(
                sender,
                sequence,
                tick,
                type,
                payload,
                signature
        );
    }
}