package core.systems;

import core.SimulationContext;
import core.commands.DamagePlayerCommand;
import core.commands.MovePlayerCommand;
import core.commands.RemoveProjectileCommand;
import core.states.LevelState;
import core.states.PlayerState;
import core.states.ProjectileState;
import core.level.TileCollisionFlags;

public final class CollisionSystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.SEQUENTIAL;
    }

    @Override
    public void update(SimulationContext context) {

        LevelState level = context.snapshot().level;

        if (level == null) return;

        // ---- Player ↔ Level ----

        for (PlayerState player : context.snapshot().players.values()) {

            if (!player.alive) continue;

            float r = player.hitboxRadius;

            float left = player.position.x - r;
            float right = player.position.x + r;
            float top = player.position.y - r;
            float bottom = player.position.y + r;

            boolean blocked = false;

            if ((level.getMaskAtWorld(left, top) & TileCollisionFlags.BLOCK_PLAYER) != 0) blocked = true;
            if ((level.getMaskAtWorld(right, top) & TileCollisionFlags.BLOCK_PLAYER) != 0) blocked = true;
            if ((level.getMaskAtWorld(left, bottom) & TileCollisionFlags.BLOCK_PLAYER) != 0) blocked = true;
            if ((level.getMaskAtWorld(right, bottom) & TileCollisionFlags.BLOCK_PLAYER) != 0) blocked = true;

            if (blocked) {
                context.addCommand(
                        new MovePlayerCommand(
                                player.id,
                                player.previousPosition.x,
                                player.previousPosition.y,
                                0f,
                                0f
                        )
                );
            }
        }

        // ---- Projectile ↔ Level + Projectile ↔ Player ----

        for (ProjectileState projectile : context.snapshot().projectiles) {

            // Projectile ↔ Level

            int mask = level.getMaskAtWorld(projectile.position.x, projectile.position.y);

            if ((mask & TileCollisionFlags.BLOCK_PROJECTILE) != 0) {
                context.addCommand(new RemoveProjectileCommand(projectile.id));
                continue;
            }

            // Projectile ↔ Player

            for (PlayerState player : context.snapshot().players.values()) {

                if (!player.alive) continue;
                if (projectile.ownerId == player.id) continue;

                float dx = projectile.position.x - player.position.x;
                float dy = projectile.position.y - player.position.y;

                float distSq = dx * dx + dy * dy;
                float radius = player.hitboxRadius;

                if (distSq <= radius * radius) {

                    context.addCommand(new DamagePlayerCommand(player.id, projectile.damage));
                    context.addCommand(new RemoveProjectileCommand(projectile.id));

                    break;
                }
            }
        }
    }
}