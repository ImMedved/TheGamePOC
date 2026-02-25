package core.commands;

public record SpawnProjectileCommand(
        long projectileId,
        long ownerId,
        int projectileTypeId,
        float x,
        float y,
        float velocityX,
        float velocityY
) implements Command {
}