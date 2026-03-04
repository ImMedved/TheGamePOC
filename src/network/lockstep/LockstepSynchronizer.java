package network.lockstep;

import network.model.NodeId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class LockstepSynchronizer {

    private final NodeId localNodeId;

    private int currentTick = 0;

    private final Map<Integer, Map<NodeId, byte[]>> inputs = new HashMap<>();

    private final BiConsumer<Integer, Map<NodeId, byte[]>> tickReadyHandler;

    public LockstepSynchronizer(
            NodeId localNodeId,
            BiConsumer<Integer, Map<NodeId, byte[]>> tickReadyHandler
    ) {

        this.localNodeId = localNodeId;
        this.tickReadyHandler = tickReadyHandler;
    }

    public synchronized void submitLocalInput(byte[] input) {

        storeInput(localNodeId, currentTick, input);

        tryAdvanceTick();
    }

    public synchronized void receiveRemoteInput(
            NodeId peerId,
            int tick,
            byte[] input
    ) {

        storeInput(peerId, tick, input);

        tryAdvanceTick();
    }

    private void storeInput(
            NodeId nodeId,
            int tick,
            byte[] input
    ) {

        Map<NodeId, byte[]> tickInputs =
                inputs.computeIfAbsent(tick, k -> new HashMap<>());

        tickInputs.put(nodeId, input);
    }

    private void tryAdvanceTick() {

        while (true) {

            Map<NodeId, byte[]> tickInputs =
                    inputs.get(currentTick);

            if (tickInputs == null)
                return;

            if (tickInputs.size() < 2)
                return;

            tickReadyHandler.accept(currentTick, tickInputs);

            inputs.remove(currentTick);

            currentTick++;
        }
    }

    public int currentTick() {
        return currentTick;
    }
}