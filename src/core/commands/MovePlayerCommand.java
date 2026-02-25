package core.commands;

public record MovePlayerCommand(
        float newX,
        float newY,
        float velocityX,
        float velocityY
) implements Command { }
