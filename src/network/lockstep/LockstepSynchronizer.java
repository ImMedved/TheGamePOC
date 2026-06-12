package network.lockstep;

import network.model.NodeId;

import java.util.HashMap;
import java.util.Map;

public final class LockstepSynchronizer {

    private final NodeId localNodeId;

    private final Map<Integer, Map<NodeId, byte[]>> inputs = new HashMap<>();

    public LockstepSynchronizer(NodeId localNodeId) {
        this.localNodeId = localNodeId;
    }

    public synchronized void submitLocalInput(int tick, byte[] input) {
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[LOCKSTEP] Local input tick=" + tick + " bytes=" + input.length
                    + " bufferedTicks=" + inputs.size());
        }
        storeInput(localNodeId, tick, input);
        notifyAll();
    }

    public synchronized void receiveRemoteInput(
            NodeId peerId,
            int tick,
            byte[] input
    ) {
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[LOCKSTEP] Remote input tick=" + tick + " from=" + peerId
                    + " bytes=" + input.length + " bufferedTicks=" + inputs.size());
        }
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

        if (tickInputs == null) {
            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[LOCKSTEP] tryGetInputs tick=" + tick + " state=missing");
            }
            return null;
        }

        if (tickInputs.size() < 2) {
            if (util.Log.isDebugEnabled()) {
                util.Log.debug("[LOCKSTEP] tryGetInputs tick=" + tick + " state=waiting size=" + tickInputs.size());
            }
            return null;
        }

        inputs.remove(tick);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[LOCKSTEP] tryGetInputs tick=" + tick + " state=ready");
        }

        return tickInputs;
    }

    public synchronized Map<NodeId, byte[]> waitForInputs(int tick) {

        while (true) {

            Map<NodeId, byte[]> tickInputs = inputs.get(tick);

            if (tickInputs != null && tickInputs.size() >= 2) {

                inputs.remove(tick);
                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[LOCKSTEP] Inputs ready tick=" + tick + " bufferedTicks=" + inputs.size());
                }
                return tickInputs;
            }

            try {
                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[LOCKSTEP] Waiting for inputs tick=" + tick + " bufferedTicks=" + inputs.size());
                }
                wait();
            } catch (InterruptedException ignored) {}
        }
    }
}
