package main;

import core.CoreEngine;
import core.config.EffectConfigs;
import core.config.ProjectileConfigs;
import core.level.LevelLoader;
import core.registries.*;
import core.states.*;
import core.systems.*;
import input.InputModule;
import network.bootstrap.NetworkBootstrap;
import network.config.NetworkConfig;
import network.node.NetworkNode;
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

        // --- Register projectiles ---

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

        float levelPixelWidth = level.width * 100f;
        float levelPixelHeight = level.height * 100f;

        float startX = levelPixelWidth * 0.5f + 500;
        float startY = levelPixelHeight * 0.5f + 500;

        // --- Player 1 ---

        PlayerState player1 = new PlayerState(1);
        player1.characterId = 0;
        player1.health = 100f;
        player1.maxHealth = 100f;
        player1.hitboxRadius = 20f;

        player1.position.set(startX, startY);
        player1.previousPosition.set(startX, startY);

        world.players.put(player1.id, player1);

        // --- Player 2 ---

        PlayerState player2 = new PlayerState(2);
        player2.characterId = 0;
        player2.health = 100f;
        player2.maxHealth = 100f;
        player2.hitboxRadius = 20f;

        player2.position.set(startX + 100, startY);
        player2.previousPosition.set(startX + 100, startY);

        world.players.put(player2.id, player2);

        // --- Systems ---

        List<GameSystem> gameSystems = List.of(
                new MovementSystem(characterRegistry),
                new ProjectileSpawnSystem(projectileRegistry),
                new ProjectileMoveSystem(),
                new AbilitySystem(),
                new EffectTickSystem(effectRegistry),
                new CollisionSystem(),
                new CameraSystem(),
                new CooldownSystem()
        );

        // --- Core Engine ---

        CoreEngine core = new CoreEngine(
                world,
                gameSystems,
                projectileRegistry
        );

        // --- Network ---

        NetworkConfig config =
                new NetworkConfig(
                        true,          // host
                        "127.0.0.1",   // peer ip
                        7777,          // local port
                        7777           // peer port
                );

        NetworkNode networkNode =
                NetworkBootstrap.start(config);

        // --- Render ---

        Path assetsRoot = Path.of("assets");
        ResourceManager resourceManager = new ResourceManager(assetsRoot);

        RenderEngine render =
                new RenderEngine(core, resourceManager, inputModule, networkNode);

        render.start();
    }
}