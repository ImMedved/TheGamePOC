package core.render;

import core.states.*;

import java.util.ArrayList;
import java.util.List;

public final class RenderSnapshotBuilder {

    private LevelRenderData cachedLevelData;

    public RenderSnapshot build(WorldState previous, WorldState current) {

        if (cachedLevelData == null && current.level != null) {

            cachedLevelData = new LevelRenderData(
                    current.level.width,
                    current.level.height,
                    current.level.textureMap
            );
        }

        List<RenderPlayer> players = buildPlayers(previous, current);
        List<RenderProjectile> projectiles = buildProjectiles(previous, current);
        List<RenderEffect> effects = buildEffects(current);
        float camX = current.camera != null ? current.camera.x : 0f;
        float camY = current.camera != null ? current.camera.y : 0f;

        return new RenderSnapshot(
                current.tickIndex,
                players,
                projectiles,
                effects,
                camX,
                camY,
                cachedLevelData
        );
    }

    private List<RenderPlayer> buildPlayers(WorldState prev, WorldState curr) {

        List<RenderPlayer> list = new ArrayList<>();

        for (PlayerState currPlayer : curr.players.values()) {

            PlayerState prevPlayer = prev.players.get(currPlayer.id);
            float prevX = prevPlayer != null ? prevPlayer.position.x : currPlayer.position.x;
            float prevY = prevPlayer != null ? prevPlayer.position.y : currPlayer.position.y;

            list.add(new RenderPlayer(
                    currPlayer.id,
                    currPlayer.characterId,
                    prevX,
                    prevY,
                    currPlayer.position.x,
                    currPlayer.position.y,
                    currPlayer.rotation,
                    currPlayer.shootCooldownRemaining,
                    currPlayer.tripleShotCooldownRemaining,
                    currPlayer.speedCooldownRemaining,
                    currPlayer.blinkCooldownRemaining,
                    currPlayer.health,
                    currPlayer.maxHealth
                    )
            );
        }
        return list;
    }

    private List<RenderProjectile> buildProjectiles(WorldState prev, WorldState curr) {

        List<RenderProjectile> list = new ArrayList<>();
        for (ProjectileState currProj : curr.projectiles) {

            ProjectileState prevProj = null;
            for (ProjectileState p : prev.projectiles) {
                if (p.id == currProj.id) {
                    prevProj = p;
                    break;
                }
            }

            float prevX = prevProj != null ? prevProj.position.x : currProj.position.x;
            float prevY = prevProj != null ? prevProj.position.y : currProj.position.y;

            PlayerState owner = curr.players.get(currProj.ownerId);
            int characterId = owner != null ? owner.characterId : 0;

            list.add(new RenderProjectile(
                    currProj.id,
                    currProj.projectileTypeId,
                    prevX,
                    prevY,
                    currProj.position.x,
                    currProj.position.y,
                    characterId
                    )
            );
        }

        return list;
    }

    private List<RenderEffect> buildEffects(WorldState curr) {

        List<RenderEffect> list = new ArrayList<>();

        for (EffectData effect : curr.effects) {

            float progress =
                    effect.duration > 0f
                            ? effect.elapsed / effect.duration
                            : 1f;

            float x = effect.position.x;
            float y = effect.position.y;

            if (effect.targetId != 0) {
                PlayerState target = curr.players.get(effect.targetId);
                if (target != null) {
                    x = target.position.x;
                    y = target.position.y;
                }
            }

            int characterId = 0;

            if (effect.sourceId != 0) {
                PlayerState source = curr.players.get(effect.sourceId);
                if (source != null) {
                    characterId = source.characterId;
                    //System.out.println(source.characterId);
                }
            }

            list.add(new RenderEffect(
                    effect.id,
                    effect.type,
                    x,
                    y,
                    progress,
                    effect.dx,
                    effect.dy,
                    effect.scaleX,
                    effect.scaleY,
                    characterId
            ));
        }

        return list;
    }
}