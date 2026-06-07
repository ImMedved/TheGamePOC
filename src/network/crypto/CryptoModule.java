package network.crypto;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public final class CryptoModule {

    private static final ThreadLocal<Signature> ED25519 =
            ThreadLocal.withInitial(() -> {
                try {
                    return Signature.getInstance("Ed25519");
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Ed25519 is not available", e);
                }
            });

    public KeyPairData generateKeyPair() {

        try {

            KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
            KeyPair pair = generator.generateKeyPair();
            return new KeyPairData(
                    pair.getPrivate(),
                    pair.getPublic()
            );

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    public byte[] sign(byte[] data, PrivateKey privateKey) {

        try {

            Signature signature = ED25519.get();

            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();

        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }

    public boolean verify(byte[] data,
                          byte[] signatureBytes,
                          PublicKey publicKey) {
        try {
            Signature signature = ED25519.get();
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Verification failed", e);
        }
    }

    public byte[] encodePublicKey(PublicKey key) {
        return key.getEncoded();
    }

    public PublicKey decodePublicKey(byte[] bytes) {

        try {
            KeyFactory factory = KeyFactory.getInstance("Ed25519");
            return factory.generatePublic(new X509EncodedKeySpec(bytes)
            );

        } catch (Exception e) {
            throw new RuntimeException("Public key decode failed", e);
        }
    }
}
