package core.registries;

public final class CharacterRegistry {

    private CharacterDefinition[] definitions;

    public CharacterRegistry(int capacity) {
        this.definitions = new CharacterDefinition[capacity];
    }

    public void register(CharacterDefinition def) {
        ensureCapacity(def.id);
        definitions[def.id] = def;
    }

    public CharacterDefinition get(int id) {
        util.Log.debug("[CHAR REG] id=" + id + " definitions.length=" + definitions.length);
        if (id < 0 || id >= definitions.length)
            throw new IllegalArgumentException("Invalid character id: " + id);

        CharacterDefinition def = definitions[id];
        if (def == null) {
            util.Log.warn("[CHAR REG] Character is not registered: " + id);
            throw new IllegalStateException("Character not registered: " + id);
        }else{
            util.Log.debug("[CHAR REG] Character resolved: " + id);
        }
        return def;
    }

    private void ensureCapacity(int id) {
        if (id >= definitions.length) {
            CharacterDefinition[] newArr = new CharacterDefinition[id + 1];
            System.arraycopy(definitions, 0, newArr, 0, definitions.length);
            definitions = newArr;
        }
    }
}