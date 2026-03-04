package core.systems;

import core.SimulationContext;
import core.commands.*;
import core.factories.EffectFactory;
import core.states.PlayerState;
import core.states.CameraState;

public final class AbilitySystem implements GameSystem {

    @Override
    public Phase phase() {
        return Phase.PARALLEL;
    }

    @Override
    public void update(SimulationContext context) {

        if (context.snapshot().players.isEmpty()) return;
        PlayerState player = context.snapshot().players.values().iterator().next();

        if (!player.alive) return;
        if (context.input().key1Pressed) castTripleShot(context, player);
        if (context.input().key2Pressed) castSpeedBoost(context, player);
        if (context.input().key3Pressed) castTeleport(context, player);

        for (PlayerState p : context.snapshot().players.values()) {
            if (p.speedBuffRemaining > 0f) {
                float newRemaining = p.speedBuffRemaining - context.dt();
                context.addCommand(new UpdateSpeedBuffCommand(p.id, newRemaining));
            }
        }
    }

    private void castTripleShot(SimulationContext context, PlayerState player) {
        if (player.tripleShotCooldownRemaining > 0f)
            return;
        CameraState cam = context.snapshot().camera;

        float worldMouseX = context.input().mouseX - cam.viewportWidth * 0.5f + cam.x;
        float worldMouseY = context.input().mouseY - cam.viewportHeight * 0.5f + cam.y;

        float dx = worldMouseX - player.position.x;
        float dy = worldMouseY - player.position.y;

        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len == 0f) return;

        dx /= len;
        dy /= len;

        spawnProjectile(context, player, dx, dy);

        float angle = (float)Math.toRadians(15);

        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);

        float dx1 = dx * cos - dy * sin;
        float dy1 = dx * sin + dy * cos;

        float dx2 = dx * cos + dy * sin;
        float dy2 = -dx * sin + dy * cos;

        spawnProjectile(context, player, dx1, dy1);
        spawnProjectile(context, player, dx2, dy2);
        player.tripleShotCooldownRemaining =
                PlayerState.TRIPLE_SHOT_COOLDOWN;
    }

    private void spawnProjectile(SimulationContext context,
                                 PlayerState player,
                                 float dirX,
                                 float dirY) {

        float speed = 600f;

        context.addCommand(new SpawnProjectileCommand(
                context.nextId(),
                player.id,
                0,
                player.position.x,
                player.position.y,
                dirX * speed,
                dirY * speed
        ));
    }

    private void castSpeedBoost(SimulationContext context, PlayerState player) {
        //System.out.println("Current multiplier: " + player.speedMultiplier);
        if (player.speedCooldownRemaining > 0f)
            return;
        context.addCommand(new ApplySpeedBoostCommand(
                player.id,
                2f,
                2f
        ));
        context.addCommand(new ApplyEffectCommand(
                EffectFactory.createSpeedEffect(
                        context.nextId(),
                        player.id,
                        player.id
                )
        ));
        player.speedCooldownRemaining =
                PlayerState.SPEED_COOLDOWN;
        //System.out.println("Updated multiplier: " + player.speedMultiplier);

    }

    private void castTeleport(SimulationContext context, PlayerState player) {
        if (player.blinkCooldownRemaining > 0f)
            return;
        CameraState cam = context.snapshot().camera;

        float worldMouseX =
                context.input().mouseX
                        - cam.viewportWidth * 0.5f
                        + cam.x;

        float worldMouseY =
                context.input().mouseY
                        - cam.viewportHeight * 0.5f
                        + cam.y;

        float dx = worldMouseX - player.position.x;
        float dy = worldMouseY - player.position.y;

        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance == 0f) return;

        float maxRange = 500f;

        float targetX;
        float targetY;

        if (distance <= maxRange) {
            targetX = worldMouseX;
            targetY = worldMouseY;
        } else {
            float nx = dx / distance;
            float ny = dy / distance;

            targetX = player.position.x + nx * maxRange;
            targetY = player.position.y + ny * maxRange;
        }

        float finalDx = targetX - player.position.x;
        float finalDy = targetY - player.position.y;

        //float length = (float)Math.sqrt(finalDx * finalDx + finalDy * finalDy);
        //float rotation = (float)Math.atan2(finalDy, finalDx);

        context.addCommand(new ApplyEffectCommand(
                EffectFactory.createDashEffect(
                        context.nextId(),
                        player.id,
                        player.position.x,
                        player.position.y,
                        finalDx,
                        finalDy
                )
        ));

        context.addCommand(new TeleportPlayerCommand(
                player.id,
                targetX,
                targetY
        ));
        player.blinkCooldownRemaining =
                PlayerState.BLINK_COOLDOWN;
    }
}