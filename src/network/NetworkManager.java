package network;

import network.queues.InboundQueue;
import network.queues.OutboundQueue;

import java.net.SocketTimeoutException;

import static core.CoreEngine.TICK_RATE;

public class NetworkManager {

    private final UdpTransport transport;
    private final InboundQueue inboundQueue;
    private final OutboundQueue outboundQueue;

    private Thread networkThread;
    private volatile boolean running = false;

    // вот эту залупу надо синхронить с тикрейтом.
    // private static final int SEND_RATE = 15;
    private static final int SEND_RATE = TICK_RATE;
    private static final long SEND_INTERVAL_MS = 1000 / SEND_RATE;

    public NetworkManager(UdpTransport transport,
                          InboundQueue inboundQueue,
                          OutboundQueue outboundQueue) {

        this.transport = transport;
        this.inboundQueue = inboundQueue;
        this.outboundQueue = outboundQueue;
    }

    public void start() {

        running = true;

        networkThread = new Thread(this::runLoop);
        networkThread.setName("NetworkThread");
        networkThread.start();
    }

    public void stop() {
        transport.getSocket().close();
        System.out.println("Network closed");
        running = false;
    }

    private void runLoop() {

        long lastSend = 0;

        try {
            transport.getSocket().setSoTimeout(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (running) {

            try {

                long now = System.currentTimeMillis();

                if (now - lastSend >= SEND_INTERVAL_MS) {
                    flushOutbound();
                    lastSend = now;
                }

                try {
                    byte[] data = transport.receive();
                    Packet packet = PacketDeserializer.deserialize(data);
                    // System.out.println("[NET] Received: " + packet.getType());
                    inboundQueue.push(packet);
                } catch (SocketTimeoutException ignored) {}

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void flushOutbound() throws Exception {

        Packet packet;

        while ((packet = outboundQueue.poll()) != null) {
            byte[] data = PacketSerializer.serialize(packet);
            // System.out.println("[NET] Sending: " + packet.getType());
            transport.send(data);
        }
    }
}
