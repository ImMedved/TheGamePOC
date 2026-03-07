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
        System.out.println("[LOCKSTEP] Local input tick=" + tick);
        storeInput(localNodeId, tick, input);
        notifyAll();
    }

    public synchronized void receiveRemoteInput(
            NodeId peerId,
            int tick,
            byte[] input
    ) {
        System.out.println("[LOCKSTEP] Remote input tick=" + tick + " from " + peerId);
        storeInput(peerId, tick, input);
        notifyAll();
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
    public synchronized Map<NodeId, byte[]> waitForInputs(int tick) {

        while (true) {

            Map<NodeId, byte[]> tickInputs = inputs.get(tick);

            if (tickInputs != null && tickInputs.size() >= 2) {

                inputs.remove(tick);
                System.out.println("[LOCKSTEP] Inputs ready for tick " + tick);
                return tickInputs;
            }

            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }
}