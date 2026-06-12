package network.adapter;

import core.states.CameraState;
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
    private final Supplier<CameraState> cameraSupplier;

    private int tick = 0;

    public NetworkInputProvider(
            NetworkNode network,
            InputModule inputModule,
            long localPlayerId,
            long remotePlayerId,
            Supplier<CameraState> cameraSupplier
    ) {

        this.network = network;
        this.inputModule = inputModule;

        this.localPlayerId = localPlayerId;
        this.remotePlayerId = remotePlayerId;
        this.cameraSupplier = cameraSupplier;
    }

    @Override
    public InputFrame get() {

        inputModule.publishSnapshot(tick);

        InputSnapshot raw = inputModule.getLatestSnapshot();
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[INPUT] raw tick=" + tick +
                    " moveX=" + raw.moveX +
                    " moveY=" + raw.moveY +
                    " shoot=" + raw.shoot +
                    " mouseX=" + raw.mouseX +
                    " mouseY=" + raw.mouseY);
        }

        CameraState camera = cameraSupplier.get();
        float worldMouseX = raw.mouseX;
        float worldMouseY = raw.mouseY;

        if (camera != null) {
            worldMouseX = raw.mouseX - camera.viewportWidth * 0.5f + camera.x;
            worldMouseY = raw.mouseY - camera.viewportHeight * 0.5f + camera.y;
        }

        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[INPUT] camera tick=" + tick +
                    " camX=" + (camera == null ? "null" : camera.x) +
                    " camY=" + (camera == null ? "null" : camera.y) +
                    " worldMouseX=" + worldMouseX +
                    " worldMouseY=" + worldMouseY);
        }

        InputSnapshot local = new InputSnapshot(
                tick,
                localPlayerId,
                raw.moveX,
                raw.moveY,
                raw.shoot,
                worldMouseX,
                worldMouseY,
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
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[NET] inputs received tick=" + tick + " size=" + inputs.size());
        }

        long end = System.nanoTime();

        double ms = (end - start) / 1_000_000.0;
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[METRIC][NET] input wait tick=" + tick + " ms=" + ms);
        }

        InputSnapshot remote = null;

        for (Map.Entry<NodeId, byte[]> entry : inputs.entrySet()) {
            if (entry.getKey().value() == remotePlayerId) {
                byte[] data = entry.getValue();
                if (util.Log.isDebugEnabled()) {
                    util.Log.debug("[NET] remote input packet tick=" + tick + " bytes=" + data.length);
                }
                remote = codec.decode(data);
            }
        }

        InputFrame frame = new InputFrame(tick);

        frame.put(local);
        if (remote == null) {
            throw new IllegalStateException("remote input missing for tick " + tick);
        }
        frame.put(remote);
        if (util.Log.isDebugEnabled()) {
            util.Log.debug("[CORE] Inputs received tick=" + tick +
                    " nextTick=" + (tick + 1) +
                    " localPlayerId=" + localPlayerId +
                    " remotePlayerId=" + remotePlayerId);
        }
        tick++;
        return frame;
    }
}
