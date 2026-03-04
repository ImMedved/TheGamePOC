package network.adapter;

import core.CoreEngine;
import core.states.WorldState;
import input.InputModule;
import input.InputSnapshot;
import network.model.NodeId;
import network.node.NetworkNode;

import java.util.Map;

public final class NetworkAdapter {

    private final NetworkNode network;
    private final CoreEngine core;
    private final InputModule input;

    private final InputCodec codec;
    private final WorldHasher hasher;

    public NetworkAdapter(
            NetworkNode network,
            CoreEngine core,
            InputModule input
    ) {

        this.network = network;
        this.core = core;
        this.input = input;

        this.codec = new InputCodec();
        this.hasher = new WorldHasher();

        network.setTickCallback(this::onTickReady);
    }

    public void update() {

        InputSnapshot snapshot =
                input.getLatestSnapshot();

        byte[] encoded =
                codec.encode(snapshot);

        network.submitLocalInput(encoded);
    }

    private void onTickReady(
            int tick,
            Map<NodeId, byte[]> inputs
    ) {

        core.forceTick();

        WorldState world =
                core.getCurrentWorld();

        byte[] hash =
                hasher.hash(world);

        network.submitStateHash(tick, hash);
    }
}