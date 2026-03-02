package core.commands;

public record UpdateSpeedBuffCommand(
        long playerId,
        float remaining
) implements Command {}
