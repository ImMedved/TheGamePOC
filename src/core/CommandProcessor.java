package core;

import core.commands.*;
import core.states.*;

import java.util.ArrayList;
import java.util.List;

public final class CommandProcessor {

    public WorldState apply(
            WorldState snapshot,
            CommandBuffer buffer
    ) {

        PlayerState newPlayer =
                snapshot.player().copy();

        List<ProjectileState> newProjectiles =
                new ArrayList<>(snapshot.projectiles());

        for (Command command : buffer.all()) {

            switch (command) {

                case MovePlayerCommand m -> {
                    newPlayer.position.set(m.newX(), m.newY());
                    newPlayer.velocity.set(
                            m.velocityX(),
                            m.velocityY()
                    );
                }

                case SpawnProjectileCommand s -> {
                    newProjectiles.add(
                            new ProjectileState(
                                    s.id(),
                                    s.x(),
                                    s.y(),
                                    s.velX(),
                                    s.velY()
                            )
                    );
                }

                case RemoveProjectileCommand r -> {
                    newProjectiles.removeIf(
                            p -> p.id == r.projectileId()
                    );
                }

                case DamagePlayerCommand d -> {
                    newPlayer.health -= d.damage();
                }
            }
        }

        return new WorldState(newPlayer, newProjectiles);
    }
}