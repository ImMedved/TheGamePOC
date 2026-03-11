package network.protocol;

import network.model.NetworkPacket;
import network.model.NodeId;

import java.io.*;
import java.util.UUID;

public final class PacketSerializer {

    public byte[] serialize(NetworkPacket packet) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeLong(packet.sender().value());
            out.writeInt(packet.sequenceNumber());
            out.writeInt(packet.tickNumber());
            out.writeInt(packet.type().ordinal());

            byte[] payload = packet.payload();
            out.writeInt(payload.length);
            out.write(payload);

            byte[] signature = packet.signature();

            if (signature == null) {
                out.writeInt(0);
            } else {
                out.writeInt(signature.length);
                out.write(signature);
            }
            out.flush();
            //System.out.println("SER payload=" + payload.length);
            //System.out.println("SER signature=" + (signature == null ? 0 : signature.length));
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NetworkPacket deserialize(byte[] data) {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream in = new DataInputStream(bais);
            NodeId sender = new NodeId(in.readLong());

            int sequence = in.readInt();
            int tick = in.readInt();
            NetworkPacket.PacketType type = NetworkPacket.PacketType.values()[in.readInt()];

            int payloadSize = in.readInt();
            byte[] payload = new byte[payloadSize];
            in.readFully(payload);
            payload = payload.clone();

            int sigSize = in.readInt();

            byte[] signature = null;

            if (sigSize > 0) {
                signature = new byte[sigSize];
                in.readFully(signature);
            }
            //System.out.println("DES payload=" + payloadSize);
            //System.out.println("DES signature=" + sigSize);
            return new NetworkPacket(
                    sender,
                    sequence,
                    tick,
                    type,
                    payload,
                    signature
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void writeUUID(DataOutputStream out, UUID id) throws IOException {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
    }

    private UUID readUUID(DataInputStream in) throws IOException {
        long msb = in.readLong();
        long lsb = in.readLong();
        return new UUID(msb, lsb);
    }
}