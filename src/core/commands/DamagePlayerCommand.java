package core.commands;

public record DamagePlayerCommand(
        float damage
) implements Command { }