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
import network.config.NetworkTopology;
import network.node.NetworkNode;
import network.node.NodeInfo;
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
import java.util.UUID;

public class Main {

    static String A_PRIVATE = "MC4CAQAwBQYDK2VwBCIEIEoLrEF7dUGY26UAgtXvj8azOq5TSFIlGeB9lrvqguct";
    static String A_PUBLIC  = "MCowBQYDK2VwAyEAcVejzRXtlfeISy5GLJIudINgt+WLEyAta4EO2gu7e2c=";

    static String B_PRIVATE = "MC4CAQAwBQYDK2VwBCIEIOb9j7pcwIpzWclwoaS3Y7jQsiFBQ/ZTvVDd7skpwEh2";
    static String B_PUBLIC  = "MCowBQYDK2VwAyEAPy8j2K+/oc3elVMGxQvW9LRe38p9h2S6xXjs0WUO/WI=";

    static String C_PRIVATE = "MC4CAQAwBQYDK2VwBCIEIPQJmA9jF+7rV6t2fAvFp8ZUBKbjrKMg09uXBJgp7waP";
    static String C_PUBLIC  = "MCowBQYDK2VwAyEAQKk7uXH7Q1Xq2Y7jZ9m+Kp8yU6cV7cYHk4Zz9y4p5m0=";

    private static ProjectileRegistry projectileRegistry;

    static void main(String[] args) throws InterruptedException {

        boolean host = hasArg(args, "--host");
        boolean validator = hasArg(args, "--validator");

        long nodeId = host ? 1 : (validator ? 3 : 2);

        NodeKeys keys = loadKeys(nodeId);
        NetworkTopology topology = createTopology();

        //System.out.println("[NODE] id=" + nodeId + " host=" + host + " validator=" + validator);

        NetworkNode networkNode = startNetwork(nodeId, host, keys, topology);

        Thread.sleep(2000);

        if (host) {
            networkNode.startGame(UUID.randomUUID(), 1, 2);
        }

        WorldState world = createWorld();

        projectileRegistry = new ProjectileRegistry(4);
        EffectRegistry effectRegistry = new EffectRegistry(4);

        EffectConfigs.registerAll(effectRegistry);
        ProjectileConfigs.registerAll(projectileRegistry);

        List<GameSystem> systems = createSystems(projectileRegistry, effectRegistry);

        InputModule inputModule = new InputModule();

        CoreEngine core = new CoreEngine(world, systems, projectileRegistry, networkNode);

        if (!validator) {
            startRender(core, inputModule, networkNode, nodeId);
        }
    }

    private static List<GameSystem> createSystems(
            ProjectileRegistry projectileRegistry,
            EffectRegistry effectRegistry
    ) {

        CharacterRegistry characterRegistry = new CharacterRegistry(4);

        CharacterDefinition c1 = new CharacterDefinition(1);
        c1.baseSpeed = 220f;
        c1.baseHealth = 100f;
        c1.baseHitboxRadius = 20f;
        characterRegistry.register(c1);

        CharacterDefinition c2 = new CharacterDefinition(2);
        c2.baseSpeed = 220f;
        c2.baseHealth = 100f;
        c2.baseHitboxRadius = 20f;
        characterRegistry.register(c2);

        System.out.println("[REGISTRY INIT] registry=" + characterRegistry.hashCode());
        return List.of(
                new MovementSystem(characterRegistry),
                new ProjectileSpawnSystem(projectileRegistry),
                new ProjectileMoveSystem(),
                new AbilitySystem(),
                new EffectTickSystem(effectRegistry),
                new CollisionSystem(),
                new CameraSystem(),
                new CooldownSystem()
        );
    }

    private static NetworkTopology createTopology() {

        PublicKey pubKey1 = loadPublic(A_PUBLIC);
        PublicKey pubKey2 = loadPublic(B_PUBLIC);
        PublicKey pubKey3 = loadPublic(C_PUBLIC);

        List<NodeInfo> nodes = List.of(
                new NodeInfo(1, "192.168.0.110", 7777, pubKey1),
                new NodeInfo(2, "192.168.0.103", 7777, pubKey2),
                new NodeInfo(3, "192.168.0.109", 7777, pubKey3)
        );

        return new NetworkTopology(nodes);
    }

    private static NetworkNode startNetwork(
            long nodeId,
            boolean host,
            NodeKeys keys,
            NetworkTopology topology
    ) {

        NetworkConfig config =
                new NetworkConfig(
                        nodeId,
                        host,
                        "192.168.0.103",
                        7777,
                        7777,
                        keys.privateKey,
                        keys.peerKey
                );

        return NetworkBootstrap.start(config, nodeId, topology);
    }

    private static WorldState createWorld() {

        WorldState world = WorldState.initial();

        LevelState level =
                LevelLoader.loadFromAscii(
                        Path.of("assets/levels/test.txt")
                );

        world.level = level;

        float startX = level.width * 50f;
        float startY = level.height * 50f;

        PlayerState p1 = new PlayerState(1);
        p1.position.set(startX, startY);

        PlayerState p2 = new PlayerState(2);
        p2.position.set(startX + 100, startY);

        world.players.put(p1.id, p1);
        world.players.put(p2.id, p2);

        return world;
    }

    private static void startRender(
            CoreEngine core,
            InputModule input,
            NetworkNode node,
            long nodeId
    ) {

        ResourceManager rm =
                new ResourceManager(Path.of("assets"));

        long localPlayer = nodeId == 1 ? 1 : 2;
        long remotePlayer = nodeId == 1 ? 2 : 1;

        RenderEngine render =
                new RenderEngine(core, rm, input, node,
                        localPlayer, remotePlayer);

        render.start();
    }

    private static boolean hasArg(String[] args, String name) {
        for (String a : args) {
            if (a.equals(name)) return true;
        }
        return false;
    }

    private static PrivateKey loadPrivate(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            KeyFactory factory = KeyFactory.getInstance("Ed25519");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey loadPublic(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            KeyFactory factory = KeyFactory.getInstance("Ed25519");
            return factory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class NodeKeys {

        final PrivateKey privateKey;
        final PublicKey peerKey;

        NodeKeys(PrivateKey privateKey, PublicKey peerKey) {
            this.privateKey = privateKey;
            this.peerKey = peerKey;
        }
    }

    private static NodeKeys loadKeys(long nodeId) {

        switch ((int) nodeId) {

            case 1 -> {
                return new NodeKeys(
                        loadPrivate(A_PRIVATE),
                        loadPublic(B_PUBLIC)
                );
            }

            case 2 -> {
                return new NodeKeys(
                        loadPrivate(B_PRIVATE),
                        loadPublic(A_PUBLIC)
                );
            }

            case 3 -> {
                return new NodeKeys(
                        loadPrivate(C_PRIVATE),
                        loadPublic(A_PUBLIC)
                );
            }

            default -> throw new IllegalStateException();
        }
    }
}