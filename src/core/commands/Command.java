package core.commands;

public sealed interface Command permits ApplyEffectCommand, ApplySpeedBoostCommand, ChangeCameraCommand, ChangeCharacterCommand, CustomCommand, DamagePlayerCommand, MovePlayerCommand, MoveProjectileCommand, RemoveEffectCommand, RemoveProjectileCommand, SpawnProjectileCommand, TeleportPlayerCommand, UpdateSpeedBuffCommand {
}