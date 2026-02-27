package core;

import core.commands.*;
import core.states.*;

import java.util.*;

public final class CommandProcessor {

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
                    ProjectileState projectile = new ProjectileState(s.projectileId());

                    projectile.ownerId = s.ownerId();
                    projectile.projectileTypeId = s.projectileTypeId();
                    projectile.position.set(s.x(), s.y());
                    projectile.previousPosition.set(s.x(), s.y());
                    projectile.velocity.set(s.velocityX(), s.velocityY());

                    next.projectiles.add(projectile);
                }

                case MoveProjectileCommand m -> {
                    for (ProjectileState p : next.projectiles) {
                        if (p.id == m.projectileId()) {
                            p.previousPosition = p.position.copy();
                            p.position.set(m.newX(), m.newY());
                            p.velocity.set(m.velocityX(), m.velocityY());
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

                case CustomCommand ignored -> {
                }
            }
        }

        next.tickIndex++;

        return next;
    }
}