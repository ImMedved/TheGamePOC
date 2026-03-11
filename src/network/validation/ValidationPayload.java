package network.validation;

import java.io.*;
import java.util.UUID;

public final class ValidationPayload {

    public final UUID gameId;
    public final int tick;
    public final boolean valid;

    public ValidationPayload(UUID gameId, int tick, boolean valid) {
        this.gameId = gameId;
        this.tick = tick;
        this.valid = valid;
    }

    public byte[] toBytes() {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeLong(gameId.getMostSignificantBits());
            out.writeLong(gameId.getLeastSignificantBits());
            out.writeInt(tick);
            out.writeBoolean(valid);

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ValidationPayload fromBytes(byte[] data) {

        try {

            DataInputStream in =
                    new DataInputStream(new ByteArrayInputStream(data));

            long msb = in.readLong();
            long lsb = in.readLong();

            UUID gameId = new UUID(msb, lsb);

            int tick = in.readInt();
            boolean valid = in.readBoolean();

            return new ValidationPayload(gameId, tick, valid);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}