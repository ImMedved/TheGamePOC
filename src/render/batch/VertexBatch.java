package render.batch;

import org.jsfml.graphics.PrimitiveType;
import org.jsfml.graphics.Vertex;
import org.jsfml.graphics.VertexArray;
import org.jsfml.system.Vector2f;
import org.jsfml.graphics.Color;

public final class VertexBatch {

    private final VertexArray vertices;

    public VertexBatch(PrimitiveType type) {
        this.vertices = new VertexArray(type);
    }

    public void clear() {
        vertices.clear();
    }

    public VertexArray getVertexArray() {
        return vertices;
    }

    public void addQuad(float x,
                        float y,
                        float width,
                        float height,
                        float u0,
                        float v0,
                        float u1,
                        float v1,
                        Color color) {

        vertices.add(new Vertex(
                new Vector2f(x, y),
                color,
                new Vector2f(u0, v0)
        ));

        vertices.add(new Vertex(
                new Vector2f(x + width, y),
                color,
                new Vector2f(u1, v0)
        ));

        vertices.add(new Vertex(
                new Vector2f(x + width, y + height),
                color,
                new Vector2f(u1, v1)
        ));

        vertices.add(new Vertex(
                new Vector2f(x, y + height),
                color,
                new Vector2f(u0, v1)
        ));
    }
}