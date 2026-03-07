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
import network.crypto.CryptoModule;
import network.crypto.KeyPairData;
import network.node.NetworkNode;
import render.RenderEngine;
import render.resources.ResourceManager;

import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
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

        // --- Network ---

        CryptoModule crypto = new CryptoModule();

        KeyPairData a = crypto.generateKeyPair();
        KeyPairData b = crypto.generateKeyPair();

        System.out.println(Base64.getEncoder().encodeToString(a.privateKey().getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(a.publicKey().getEncoded()));

        System.out.println(Base64.getEncoder().encodeToString(b.privateKey().getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(b.publicKey().getEncoded()));

        PrivateKey privateKey = loadPrivate(A_PRIVATE);
        PublicKey peerKey = loadPublic(B_PUBLIC);

        NetworkConfig config =
                new NetworkConfig(
                        true,
                        "192.168.0.103",
                        7777,
                        7777,
                        localKeys.privateKey(),
                        peerKeys.publicKey()
                );

        NetworkNode networkNode = NetworkBootstrap.start(config);

        // --- Core Engine ---

        CoreEngine core = new CoreEngine(
                world,
                gameSystems,
                projectileRegistry,
                networkNode
        );

        // --- Render ---

        Path assetsRoot = Path.of("assets");
        ResourceManager resourceManager = new ResourceManager(assetsRoot);

        RenderEngine render = new RenderEngine(core, resourceManager, inputModule, networkNode);

        render.start();
    }
    static PrivateKey loadPrivate(String base64) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);

        KeyFactory factory = KeyFactory.getInstance("Ed25519");

        return factory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    static PublicKey loadPublic(String base64) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);

        KeyFactory factory = KeyFactory.getInstance("Ed25519");

        return factory.generatePublic(new X509EncodedKeySpec(bytes));
    }
}