package core.registries;

public final class EffectRegistry {

    private EffectDefinition[] definitions;

    public EffectRegistry(int capacity) {
        this.definitions = new EffectDefinition[capacity];
    }

    public void register(EffectDefinition def) {
        ensureCapacity(def.id);
        definitions[def.id] = def;
    }

    public EffectDefinition get(int id) {
        if (id < 0 || id >= definitions.length)
            throw new IllegalArgumentException("Invalid effect id: " + id);

        EffectDefinition def = definitions[id];
        if (def == null)
            throw new IllegalStateException("Effect not registered: " + id);

        return def;
    }

    private void ensureCapacity(int id) {
        if (id >= definitions.length) {
            EffectDefinition[] newArr =
                    new EffectDefinition[id + 1];
            System.arraycopy(definitions, 0, newArr, 0, definitions.length);
            definitions = newArr;
        }
    }
}