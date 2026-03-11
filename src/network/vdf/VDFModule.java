package network.vdf;

import java.math.BigInteger;
import java.security.MessageDigest;

public final class VDFModule {

    private static final BigInteger MODULUS =
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    private static final int ITERATIONS = 200_000;

    public byte[] compute(byte[] seed) {

        BigInteger x = new BigInteger(1, seed);

        for (int i = 0; i < ITERATIONS; i++) {
            x = x.multiply(x).mod(MODULUS);
        }

        return hash(x.toByteArray());
    }

    private byte[] hash(byte[] data) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}