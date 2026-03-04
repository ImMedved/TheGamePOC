package network.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public final class KeyPairData {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public KeyPairData(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }
}