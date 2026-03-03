package render.resources;

import org.jsfml.graphics.Texture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    private final Path rootPath;
    private final Map<String, Texture> textures = new HashMap<>();

    public ResourceManager(Path rootPath) {
        this.rootPath = rootPath;
    }

    public Texture getTexture(String key) {
        Texture texture = textures.get(key);
        if (texture != null) return texture;

        Texture loaded = loadTexture(key);
        textures.put(key, loaded);
        return loaded;
    }

    private Texture loadTexture(String key) {
        try {
            Texture texture = new Texture();
            texture.loadFromFile(rootPath.resolve(key));
            texture.setRepeated(false);
            texture.setSmooth(false);
            return texture;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + key, e);
        }
    }

    public void clear() {
        textures.clear();
    }
}