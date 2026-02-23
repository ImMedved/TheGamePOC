package core;

import network.Packet;

public interface NetworkBridge {

    void send(Packet packet);

    Packet poll();
}
