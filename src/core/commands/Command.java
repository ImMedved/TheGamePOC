package core.commands;

public sealed interface Command permits ApplyEffectCommand, ChangeCameraCommand, ChangeCharacterCommand, CustomCommand, DamagePlayerCommand, MovePlayerCommand, MoveProjectileCommand, RemoveEffectCommand, RemoveProjectileCommand, SpawnProjectileCommand {
}