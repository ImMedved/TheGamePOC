package network.adapter;

import input.InputFrame;
import input.InputModule;
import input.InputSnapshot;
import network.model.NodeId;
import network.node.NetworkNode;

import java.util.Map;
import java.util.function.Supplier;

public final class NetworkInputProvider implements Supplier<InputFrame> {

    private final NetworkNode network;
    private final InputModule inputModule;

    private final InputCodec codec = new InputCodec();

    private final long localPlayerId;
    private final long remotePlayerId;

    private int tick = 0;

    public NetworkInputProvider(
            NetworkNode network,
            InputModule inputModule,
            long localPlayerId,
            long remotePlayerId
    ) {

        this.network = network;
        this.inputModule = inputModule;

        this.localPlayerId = localPlayerId;
        this.remotePlayerId = remotePlayerId;
    }

    @Override
    public InputFrame get() {

        inputModule.publishSnapshot(tick);

        InputSnapshot raw = inputModule.getLatestSnapshot();

        InputSnapshot local = new InputSnapshot(
                tick,
                localPlayerId,
                raw.moveX,
                raw.moveY,
                raw.shoot,
                raw.mouseX,
                raw.mouseY,
                raw.key1Pressed,
                raw.key2Pressed,
                raw.key3Pressed
        );

        network.submitLocalInput(
                tick,
                codec.encode(local)
        );

        long start = System.nanoTime();

        Map<NodeId, byte[]> inputs = network.waitForInputs(tick);

        long end = System.nanoTime();

        double ms = (end - start) / 1_000_000.0;
        System.out.println("[METRIC][NET] input wait = " + ms + " ms");

        InputSnapshot remote = null;

        for (Map.Entry<NodeId, byte[]> entry : inputs.entrySet()) {

            if (entry.getKey().value() == remotePlayerId) {
                remote = codec.decode(entry.getValue());
            }
        }

        InputFrame frame = new InputFrame(tick);

        frame.put(local);
        if (remote == null)
            throw new IllegalStateException("писяпопа");
        frame.put(remote);
        tick++;
        System.out.println("[CORE] Inputs received for tick " + tick);
        return frame;
    }
}