package core.commands;

public record RemoveEffectCommand(
        long effectId
) implements Command {
}