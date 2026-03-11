package network.vdf;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

public final class VDFSeed {

    public static byte[] create(UUID gameId, List<Long> nodeIds) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(gameId.getMostSignificantBits());
            buffer.putLong(gameId.getLeastSignificantBits());

            digest.update(buffer.array());

            for (Long id : nodeIds) {

                ByteBuffer b = ByteBuffer.allocate(8);
                b.putLong(id);
                digest.update(b.array());
            }

            return digest.digest();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}