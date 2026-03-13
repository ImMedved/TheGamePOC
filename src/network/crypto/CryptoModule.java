package network.crypto;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public final class CryptoModule {

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

            Signature signature = Signature.getInstance("Ed25519");

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
        //System.out.println("[CORE] signatureBytes length is: " + signatureBytes.length);

        try {
            Signature signature = Signature.getInstance("Ed25519");
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