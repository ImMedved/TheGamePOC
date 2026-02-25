package network.queues;

import network.packets.Packet;

import java.util.concurrent.ConcurrentLinkedQueue;

public class InboundQueue {

    private final ConcurrentLinkedQueue<Packet> queue =
            new ConcurrentLinkedQueue<>();

    public void push(Packet packet) {
        queue.add(packet);
    }

    public Packet poll() {
        return queue.poll();
    }
}
