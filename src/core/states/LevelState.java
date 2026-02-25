package core.states;

import java.util.HashMap;
import java.util.Map;

public final class LevelState {

    public int width;
    public int height;

    public Object collisionData;

    public final Map<String, Object> attributes = new HashMap<>();
}
