package network.validation;

import network.model.NodeId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class StateHashValidator {

    private final int hashInterval;

    private final Map<Integer, byte[]> localHashes = new HashMap<>();

    private final Map<NodeId, Map<Integer, byte[]>> remoteHashes = new HashMap<>();

    private final Consumer<Integer> desyncHandler;

    public StateHashValidator(
            int hashInterval,
            Consumer<Integer> desyncHandler
    ) {
        this.hashInterval = hashInterval;
        this.desyncHandler = desyncHandler;
    }

    public boolean shouldHash(int tick) {
        return tick % hashInterval == 0;
    }

    public synchronized void storeLocalHash(int tick, byte[] hash) {

        localHashes.put(tick, hash);

        validate(tick);
    }

    public synchronized void receiveRemoteHash(
            NodeId peer,
            int tick,
            byte[] hash
    ) {

        Map<Integer, byte[]> peerHashes =
                remoteHashes.computeIfAbsent(peer, k -> new HashMap<>());

        peerHashes.put(tick, hash);

        validate(tick);
    }

    private void validate(int tick) {

        byte[] local = localHashes.get(tick);

        if (local == null)
            return;

        for (Map<Integer, byte[]> peerHashes : remoteHashes.values()) {

            byte[] remote = peerHashes.get(tick);

            if (remote == null)
                continue;

            if (!equals(local, remote)) {

                desyncHandler.accept(tick);

                return;
            }
        }
    }

    private boolean equals(byte[] a, byte[] b) {

        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; i++) {

            if (a[i] != b[i])
                return false;
        }

        return true;
    }
}