package network.model;

import java.io.*;
import java.util.UUID;

public final class GameStartPayload {

    public final UUID gameId;
    public final long playerA;
    public final long playerB;

    public GameStartPayload(UUID gameId, long playerA, long playerB) {
        this.gameId = gameId;
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public byte[] toBytes() {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeLong(gameId.getMostSignificantBits());
            out.writeLong(gameId.getLeastSignificantBits());
            out.writeLong(playerA);
            out.writeLong(playerB);

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GameStartPayload fromBytes(byte[] data) {

        try {

            DataInputStream in =
                    new DataInputStream(new ByteArrayInputStream(data));

            long msb = in.readLong();
            long lsb = in.readLong();

            UUID gameId = new UUID(msb, lsb);

            long playerA = in.readLong();
            long playerB = in.readLong();

            return new GameStartPayload(gameId, playerA, playerB);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}