package network.model;

public enum PacketType {

    HELLO,
    HELLO_ACK,
    READY,

    INPUT,

    STATE_HASH,

    VOID_GAME,

    PING,
    PONG
}