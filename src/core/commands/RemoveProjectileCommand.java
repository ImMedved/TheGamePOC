package core.commands;

public record RemoveProjectileCommand(
        long projectileId
) implements Command {
}