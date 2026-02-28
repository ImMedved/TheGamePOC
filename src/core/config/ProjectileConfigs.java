package core.config;

import core.registries.ProjectileDefinition;
import core.registries.ProjectileRegistry;

public final class ProjectileConfigs {

    public static void registerAll(ProjectileRegistry registry) {

        ProjectileDefinition bullet = new ProjectileDefinition(0);

        bullet.speed = 500f;
        bullet.baseDamage = 10f;
        bullet.hitboxRadius = 5f;

        bullet.maxDistance = 600f;
        bullet.lifetime = 1f;

        registry.register(bullet);
    }
}