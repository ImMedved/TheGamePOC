package core.commands;

public record TeleportPlayerCommand(
        long playerId,
        float x,
        float y
) implements Command {}