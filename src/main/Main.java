package main;

import core.CoreEngine;
import core.config.EffectConfigs;
import core.config.ProjectileConfigs;
import core.level.LevelLoader;
import core.registries.*;
import core.states.*;
import core.systems.*;
import input.InputModule;
import render.RenderEngine;
import render.resources.ResourceManager;

import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        InputModule inputModule = new InputModule();

        // --- Registries ---

        CharacterRegistry characterRegistry = new CharacterRegistry(4);
        ProjectileRegistry projectileRegistry = new ProjectileRegistry(4);
        EffectRegistry effectRegistry = new EffectRegistry(4);
        EffectConfigs.registerAll(effectRegistry);

        // --- Register default character ---

        CharacterDefinition defaultCharacter = new CharacterDefinition(0);

        defaultCharacter.baseSpeed = 300f;
        defaultCharacter.baseHealth = 100f;
        defaultCharacter.baseHitboxRadius = 20f;

        characterRegistry.register(defaultCharacter);

        // --- Register default projectile ---

        ProjectileConfigs.registerAll(projectileRegistry);

        // --- Create initial world ---

        WorldState world = WorldState.initial();

        LevelState level = LevelLoader.loadFromAscii(Path.of("assets/levels/test.txt"));
        world.level = level;

        CharacterDefinition c1 = new CharacterDefinition(1);
        c1.baseSpeed = 250f;
        c1.baseHealth = 100f;
        c1.baseHitboxRadius = 20f;
        characterRegistry.register(c1);

        CharacterDefinition c2 = new CharacterDefinition(2);
        c2.baseSpeed = 250f;
        c2.baseHealth = 100f;
        c2.baseHitboxRadius = 20f;
        characterRegistry.register(c2);

        PlayerState player = new PlayerState(1);
        player.characterId = 0;
        player.health = 100f;
        player.maxHealth = 100f;
        player.hitboxRadius = 20f;

        float levelPixelWidth = level.width * 100f;
        float levelPixelHeight = level.height * 100f;

        float startX = levelPixelWidth * 0.5f;
        float startY = levelPixelHeight * 0.5f;

        player.position.set(startX, startY);
        player.previousPosition.set(startX, startY);

        world.players.put(player.id, player);

        // --- Systems ---

        List<GameSystem> gameSystems = List.of(
                new MovementSystem(characterRegistry),
                new ProjectileSpawnSystem(projectileRegistry),
                new ProjectileMoveSystem(),
                new AbilitySystem(),
                new EffectTickSystem(effectRegistry),
                new CollisionSystem(),
                new CameraSystem()
        );

        // --- Core Engine ---

        CoreEngine core = new CoreEngine(
                inputModule,
                world,
                gameSystems,
                projectileRegistry
        );

        // core.start();

        Path assetsRoot = Path.of("assets");
        ResourceManager resourceManager = new ResourceManager(assetsRoot);

        RenderEngine render = new RenderEngine(core, resourceManager, inputModule);
        render.start();
    }
}