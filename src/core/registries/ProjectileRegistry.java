package core.registries;

public final class ProjectileRegistry {

    private ProjectileDefinition[] definitions;

    public ProjectileRegistry(int capacity) {
        this.definitions = new ProjectileDefinition[capacity];
    }

    public void register(ProjectileDefinition def) {
        ensureCapacity(def.id);
        definitions[def.id] = def;
    }

    public ProjectileDefinition get(int id) {
        if (id < 0 || id >= definitions.length)
            throw new IllegalArgumentException("Invalid projectile id: " + id);

        ProjectileDefinition def = definitions[id];
        if (def == null)
            throw new IllegalStateException("Projectile not registered: " + id);

        return def;
    }

    private void ensureCapacity(int id) {
        if (id >= definitions.length) {
            ProjectileDefinition[] newArr =
                    new ProjectileDefinition[id + 1];
            System.arraycopy(definitions, 0, newArr, 0, definitions.length);
            definitions = newArr;
        }
    }
}