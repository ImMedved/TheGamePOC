package network.lockstep;

import network.model.NodeId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class LockstepSynchronizer {

    private final NodeId localNodeId;

    private final Map<Integer, Map<NodeId, byte[]>> inputs = new HashMap<>();

    public LockstepSynchronizer(NodeId localNodeId) {
        this.localNodeId = localNodeId;
    }

    public synchronized void submitLocalInput(int tick, byte[] input) {
        storeInput(localNodeId, tick, input);
    }

    public synchronized void receiveRemoteInput(
            NodeId peerId,
            int tick,
            byte[] input
    ) {
        storeInput(peerId, tick, input);
    }

    private void storeInput(
            NodeId nodeId,
            int tick,
            byte[] input
    ) {

        Map<NodeId, byte[]> tickInputs = inputs.computeIfAbsent(tick, k -> new HashMap<>());

        tickInputs.put(nodeId, input);
    }

    public synchronized Map<NodeId, byte[]> tryGetInputs(int tick) {

        Map<NodeId, byte[]> tickInputs = inputs.get(tick);

        if (tickInputs == null)
            return null;

        if (tickInputs.size() < 2)
            return null;

        inputs.remove(tick);

        return tickInputs;
    }
}