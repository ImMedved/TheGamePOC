package core.commands;

public record MoveProjectileCommand(
        long projectileId,
        float newX,
        float newY,
        float velocityX,
        float velocityY,
        float elapsed,
        float traveledDistance
) implements Command {}