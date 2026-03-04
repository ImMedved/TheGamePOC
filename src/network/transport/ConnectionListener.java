package network.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public final class ConnectionListener {

    private final ServerSocket serverSocket;

    private volatile boolean running;

    private Thread acceptThread;

    public ConnectionListener(int port) {

        try {

            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            throw new RuntimeException("Listener init failed", e);
        }
    }

    public void start(Consumer<Socket> connectionHandler) {

        running = true;

        acceptThread = new Thread(() -> {

            while (running) {

                try {

                    Socket socket = serverSocket.accept();

                    connectionHandler.accept(socket);

                } catch (IOException e) {

                    if (running)
                        e.printStackTrace();
                }
            }

        }, "ConnectionListener");

        acceptThread.start();
    }

    public void stop() {

        running = false;

        try {
            serverSocket.close();
        } catch (IOException ignored) {}

    }
}