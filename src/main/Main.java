package main;

/*
Запускать с --enable-native-access=ALL-UNNAMED
 */

import core.CoreEngine;
import core.states.WorldState;
import input.InputModule;
import network.helpers.CoreNetworkBridge;
import network.NetworkManager;
import network.helpers.UdpTransport;
import network.queues.InboundQueue;
import network.queues.OutboundQueue;
import render.RenderEngine;

public class Main {
    /**
     * для запуска нужно:
     * win 11
     * отключить фаервол
     * переключить сеть в домашний режим
     * узнать адреса обоих компов
     * в код вписать localId для каждого свой, ip противоположного компа в каждый клиент
     * порт на одном будет 5000/5001, на другом 5001/5000, jsfml
     */

    /**
     * To run, you need:
     * Windows 11
     * Disable firewall
     * Switch the network to home mode
     * Addresses of both computers
     * Enter the localId for each computer into the code, and the IP of the opposite computer into each client
     * The port on one will be in/out 5000/5001, on the other 5001/5000
     * !! Go to Project Structure -> Libraries -> New -> Add jsfml.jar from root project folder (src in the same place)
     *      -> apply
     */
    public static void main(String[] args) {
        // заглушки, временно
        int localPort = 5000;
        String remoteIp = "192.168.xxx.xxx"; // IP второго компа
        int remotePort = 5001;
        // позже нужно добавить ввод этого говна с ui, а локалку подсосать с системы

        InputModule inputModule = new InputModule();
        int localPlayerId = 1;

        InboundQueue inbound = new InboundQueue();
        OutboundQueue outbound = new OutboundQueue();

        UdpTransport transport = null;

        try {
            transport = new UdpTransport(localPort, remoteIp, remotePort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // NetworkManager networkManager = new NetworkManager(transport, inbound, outbound);

        // CoreNetworkBridge bridge = new CoreNetworkBridge(inbound, outbound);

        WorldState initial = WorldState.initial();

        CoreEngine core =
                new CoreEngine(inputModule, initial);

        // networkManager.start();
        core.start();
        RenderEngine render = new RenderEngine(core, inputModule);

        render.start();
        core.start();
    }
}

