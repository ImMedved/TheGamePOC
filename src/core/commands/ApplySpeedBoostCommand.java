package core.commands;

public record ApplySpeedBoostCommand(
        long playerId,
        float multiplier,
        float duration
) implements Command {}
