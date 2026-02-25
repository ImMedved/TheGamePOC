package network.helpers;

import java.net.*;
import java.util.Arrays;

public class UdpTransport {

    private final DatagramSocket socket;
    private final InetAddress remoteAddress;
    private final int remotePort;

    public UdpTransport(int localPort, String remoteIp, int remotePort) throws Exception {
        this.socket = new DatagramSocket(localPort);
        this.remoteAddress = InetAddress.getByName(remoteIp);
        this.remotePort = remotePort;
    }

    public void send(byte[] data) throws Exception {
        DatagramPacket packet =
                new DatagramPacket(data, data.length, remoteAddress, remotePort);
        socket.send(packet);
    }

    public byte[] receive() throws Exception {
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return Arrays.copyOf(packet.getData(), packet.getLength());
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
