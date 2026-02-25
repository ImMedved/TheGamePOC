package core.networkCore;

import network.packets.Packet;

public interface NetworkBridge {

    void send(Packet packet);

    Packet poll();
}
