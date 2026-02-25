package core.commands;

public record ChangeCharacterCommand(
        long playerId,
        int newCharacterId
) implements Command {
}