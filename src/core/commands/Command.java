package core.commands;

public sealed interface Command permits
        MovePlayerCommand,
        SpawnProjectileCommand,
        RemoveProjectileCommand,
        DamagePlayerCommand {
}
