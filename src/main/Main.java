package main;

import core.CoreEngine;
import core.registries.*;
import core.states.*;
import core.systems.*;
import input.InputModule;
import render.RenderEngine;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        InputModule inputModule = new InputModule();

        // --- Registries ---

        CharacterRegistry characterRegistry =
                new CharacterRegistry(4);

        ProjectileRegistry projectileRegistry =
                new ProjectileRegistry(4);

        EffectRegistry effectRegistry =
                new EffectRegistry(4);

        // --- Register default character ---

        CharacterDefinition defaultCharacter =
                new CharacterDefinition(0);

        defaultCharacter.baseSpeed = 300f;
        defaultCharacter.baseHealth = 100f;
        defaultCharacter.baseHitboxRadius = 20f;

        characterRegistry.register(defaultCharacter);

        // --- Register default projectile ---

        ProjectileDefinition defaultProjectile =
                new ProjectileDefinition(0);

        defaultProjectile.speed = 500f;
        defaultProjectile.baseDamage = 10f;
        defaultProjectile.lifetime = 2f;
        defaultProjectile.hitboxRadius = 5f;

        projectileRegistry.register(defaultProjectile);

        // --- Create initial world ---

        WorldState world = WorldState.initial();

        LevelState level = new LevelState(1000, 1000);
        world.level = level;

        PlayerState player = new PlayerState(1);
        player.characterId = 0;
        player.health = 100f;
        player.maxHealth = 100f;
        player.hitboxRadius = 20f;

        world.players.put(player.id, player);

        // --- Systems ---

        List<core.systems.System> systems = List.of(
                new MovementSystem(characterRegistry),
                new ProjectileSpawnSystem(projectileRegistry),
                new ProjectileMoveSystem(),
                new EffectTickSystem(effectRegistry),
                new CollisionSystem()
        );

        // --- Core Engine ---

        CoreEngine core =
                new CoreEngine(inputModule, world, systems);

        core.start();

        RenderEngine render = new RenderEngine(core);

        render.start();
    }
}