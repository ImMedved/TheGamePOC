package network.transport;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public final class P2PConnection {

    private final Socket socket;

    private final DataInputStream in;
    private final DataOutputStream out;

    private volatile boolean running;

    private Thread receiveThread;

    public P2PConnection(Socket socket) {

        try {

            this.socket = socket;

            this.in =
                    new DataInputStream(
                            new BufferedInputStream(socket.getInputStream())
                    );

            this.out =
                    new DataOutputStream(
                            new BufferedOutputStream(socket.getOutputStream())
                    );

        } catch (IOException e) {
            throw new RuntimeException("Connection init failed", e);
        }
    }

    public void startReceiving(Consumer<byte[]> packetHandler) {

        running = true;

        receiveThread = new Thread(() -> {

            try {

                while (running) {

                    int length = in.readInt();

                    byte[] data = new byte[length];

                    in.readFully(data);

                    packetHandler.accept(data);
                    System.out.println("Triggered packetHandler.accept(data) from P2PConnection.startReceiving");
                    System.out.println("[NET] Transport received bytes=" + data.length);
                }

            } catch (IOException e) {

                if (running) {
                    e.printStackTrace();
                }

            }

        }, "P2PReceiveThread");

        receiveThread.start();
    }

    public synchronized void send(byte[] data) {

        try {
            System.out.println("[NET] Transport send bytes=" + data.length);
            out.writeInt(data.length);

            out.write(data);

            out.flush();

        } catch (IOException e) {
            throw new RuntimeException("Send failed", e);
        }
    }

    public void close() {

        running = false;

        try {
            socket.close();
        } catch (IOException ignored) {}

    }
}