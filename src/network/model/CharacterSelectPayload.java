package network.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class CharacterSelectPayload {

    public final long playerId;
    public final int characterId;

    public CharacterSelectPayload(long playerId, int characterId) {
        this.playerId = playerId;
        this.characterId = characterId;
    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeLong(playerId);
            out.writeInt(characterId);
            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CharacterSelectPayload fromBytes(byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            return new CharacterSelectPayload(in.readLong(), in.readInt());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
