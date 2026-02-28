package core.config;

public final class ProjectileEffectConfigs {

    public static int getOnExpireEffect(int projectileTypeId) {

        return switch (projectileTypeId) {
            case 0 -> EffectConfigs.BULLET_HOLE;
            default -> -1;
        };
    }
}