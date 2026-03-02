package input;

class LiveInputState {

    volatile boolean wDown;
    volatile boolean aDown;
    volatile boolean sDown;
    volatile boolean dDown;

    volatile boolean lmbDown;
    volatile boolean prevLmbDown;

    volatile boolean key1Down;
    volatile boolean key2Down;
    volatile boolean key3Down;

    volatile boolean prevKey1Down;
    volatile boolean prevKey2Down;
    volatile boolean prevKey3Down;

    volatile int mouseX;
    volatile int mouseY;
}