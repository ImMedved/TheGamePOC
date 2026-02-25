package core.commands;

public sealed interface Command permits
        MovePlayerCommand,
        ChangeCharacterCommand,
        SpawnProjectileCommand,
        MoveProjectileCommand,
        RemoveProjectileCommand,
        DamagePlayerCommand,
        ApplyEffectCommand,
        RemoveEffectCommand,
        CustomCommand {
}