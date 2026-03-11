package network.consensus;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;

public final class ValidatorSelector {

    public long selectValidator(byte[] entropy, List<Long> nodeIds) {

        long bestNode = -1;
        byte[] bestScore = null;

        for (long nodeId : nodeIds) {

            byte[] score = score(entropy, nodeId);

            if (bestScore == null || compare(score, bestScore) > 0) {
                bestScore = score;
                bestNode = nodeId;
            }
        }

        return bestNode;
    }

    private byte[] score(byte[] entropy, long nodeId) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(entropy);

            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(nodeId);

            digest.update(buffer.array());

            return digest.digest();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int compare(byte[] a, byte[] b) {

        for (int i = 0; i < a.length; i++) {

            int diff = (a[i] & 0xFF) - (b[i] & 0xFF);

            if (diff != 0) return diff;
        }

        return 0;
    }
}