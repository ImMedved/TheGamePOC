package core.commands;

public record SpawnProjectileCommand(
        long id,
        float x,
        float y,
        float velX,
        float velY
) implements Command { }
