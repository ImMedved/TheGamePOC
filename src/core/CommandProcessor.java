package core;

import core.commands.*;
import core.registries.ProjectileDefinition;
import core.registries.ProjectileRegistry;
import core.states.*;

import java.util.*;

public final class CommandProcessor {

    private final ProjectileRegistry projectileRegistry;

    public CommandProcessor(ProjectileRegistry projectileRegistry) {
        this.projectileRegistry = projectileRegistry;
    }

    public WorldState apply(WorldState snapshot, List<Command> commands) {

        WorldState next = snapshot.copy();

        for (Command command : commands) {

            switch (command) {

                case MovePlayerCommand m -> {
                    PlayerState player = next.players.get(m.playerId());
                    if (player != null) {
                        player.previousPosition = player.position.copy();
                        player.position.set(m.newX(), m.newY());
                        player.velocity.set(m.velocityX(), m.velocityY());
                    }
                }

                case ChangeCharacterCommand c -> {
                    PlayerState player = next.players.get(c.playerId());
                    if (player != null) {
                        player.characterId = c.newCharacterId();
                    }
                }

                case SpawnProjectileCommand s -> {
                    ProjectileDefinition def = projectileRegistry.get(s.projectileTypeId());
                    ProjectileState projectile = new ProjectileState(s.projectileId());

                    projectile.ownerId = s.ownerId();
                    projectile.projectileTypeId = s.projectileTypeId();
                    projectile.position.set(s.x(), s.y());
                    projectile.previousPosition.set(s.x(), s.y());
                    projectile.velocity.set(s.velocityX(), s.velocityY());

                    projectile.hitboxRadius = def.hitboxRadius;
                    projectile.damage = def.baseDamage;
                    projectile.lifetime = def.lifetime;   // <- ВОТ ЭТО КЛЮЧЕВОЕ
                    projectile.elapsed = 0f;
                    projectile.maxDistance = def.maxDistance;
                    projectile.traveledDistance = 0f;

                    next.projectiles.add(projectile);
                }

                case MoveProjectileCommand m -> {
                    for (ProjectileState p : next.projectiles) {
                        if (p.id == m.projectileId()) {
                            p.previousPosition = p.position.copy();
                            p.position.set(m.newX(), m.newY());
                            p.velocity.set(m.velocityX(), m.velocityY());
                            p.elapsed = m.elapsed();
                            p.traveledDistance = m.traveledDistance();
                            break;
                        }
                    }
                }

                case RemoveProjectileCommand r -> {
                    next.projectiles.removeIf(p -> p.id == r.projectileId());
                }

                case DamagePlayerCommand d -> {
                    PlayerState player = next.players.get(d.playerId());
                    if (player != null) {
                        player.health -= d.damage();
                        if (player.health <= 0f) {
                            player.alive = false;
                        }
                    }
                }

                case ApplyEffectCommand e -> {
                    next.effects.add(e.effect());
                }

                case RemoveEffectCommand r -> {
                    next.effects.removeIf(effect -> effect.id == r.effectId());
                }

                case ChangeCameraCommand c -> {
                    next.camera.x = c.x();
                    next.camera.y = c.y();
                }

                case ApplySpeedBoostCommand c -> {
                    PlayerState p = next.players.get(c.playerId());
                    if (p != null) {
                        p.speedMultiplier = c.multiplier();
                        p.speedBuffRemaining = c.duration();
                    }
                    System.out.println("[CORE] Triggered ApplySpeedBoostCommand");
                }

                case TeleportPlayerCommand c -> {
                    PlayerState p = next.players.get(c.playerId());
                    if (p != null) {
                        p.previousPosition = p.position.copy();
                        p.position.set(c.x(), c.y());
                    }
                    System.out.println("[CORE] Triggered TeleportPlayerCommand");

                }

                case UpdateSpeedBuffCommand c -> {
                    PlayerState p = next.players.get(c.playerId());
                    if (p != null) {
                        p.speedBuffRemaining = Math.max(0f, c.remaining());
                        if (p.speedBuffRemaining == 0f) {
                            p.speedMultiplier = 1f;
                        }
                    }
                    System.out.println("[CORE] Triggered UpdateSpeedBuffCommand");

                }

                case CustomCommand ignored -> {
                }
            }
        }

        next.tickIndex++;

        return next;
    }
}