package network;

import core.NetworkBridge;
import network.queues.InboundQueue;
import network.queues.OutboundQueue;

public class CoreNetworkBridge implements NetworkBridge {

    private final InboundQueue inbound;
    private final OutboundQueue outbound;

    public CoreNetworkBridge(InboundQueue inbound,
                             OutboundQueue outbound) {

        this.inbound = inbound;
        this.outbound = outbound;
    }

    @Override
    public void send(Packet packet) {
        outbound.push(packet);
    }

    @Override
    public Packet poll() {
        return inbound.poll();
    }
}
