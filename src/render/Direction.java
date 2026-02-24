package render;

public enum Direction {
    DOWN(0),
    LEFT(1),
    RIGHT(2),
    UP(3);

    public final int row;

    Direction(int row) {
        this.row = row;
    }
}