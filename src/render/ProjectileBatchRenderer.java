package render;

import core.*;
import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProjectileBatchRenderer {

    private static final float BULLET_WIDTH = 13f;
    private static final float BULLET_HEIGHT = 40f;

    private final Texture bulletTexture;
    private final Texture holeTexture;

    private final VertexArray bulletVertices;
    private final VertexArray holeVertices;

    public ProjectileBatchRenderer() {

        bulletTexture = new Texture();
        holeTexture = new Texture();

        try {
            bulletTexture.loadFromFile(Paths.get("assets/bullet.png"));
            holeTexture.loadFromFile(Paths.get("assets/bulletHole.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bulletVertices = new VertexArray(PrimitiveType.QUADS);
        holeVertices = new VertexArray(PrimitiveType.QUADS);
    }

    public void draw(RenderWindow window,
                     List<ProjectileState> projectiles,
                     List<BulletHoleState> holes,
                     float dt) {

        bulletVertices.clear();
        holeVertices.clear();

        buildBullets(projectiles);
        buildHoles(holes, dt);

        window.draw(bulletVertices, new RenderStates(bulletTexture));
        window.draw(holeVertices, new RenderStates(holeTexture));
    }

    private void buildBullets(List<ProjectileState> projectiles) {

        for (ProjectileState p : projectiles) {

            float angle = (float)Math.atan2(p.dirY, p.dirX);

            float centerX = p.x;
            float centerY = p.y;

            float halfW = BULLET_WIDTH / 2f;
            float halfH = BULLET_HEIGHT / 2f;

            Vector2f[] quad = {
                    new Vector2f(-halfW, -halfH),
                    new Vector2f(halfW, -halfH),
                    new Vector2f(halfW, halfH),
                    new Vector2f(-halfW, halfH)
            };

            for (int i = 0; i < 4; i++) {

                float rotatedX =
                        (float)(quad[i].x * Math.cos(angle) -
                                quad[i].y * Math.sin(angle));

                float rotatedY =
                        (float)(quad[i].x * Math.sin(angle) +
                                quad[i].y * Math.cos(angle));

                quad[i] = new Vector2f(
                        rotatedX + centerX,
                        rotatedY + centerY
                );
            }

            bulletVertices.add(new Vertex(quad[0], new Vector2f(0,0)));
            bulletVertices.add(new Vertex(quad[1], new Vector2f(BULLET_WIDTH,0)));
            bulletVertices.add(new Vertex(quad[2], new Vector2f(BULLET_WIDTH,BULLET_HEIGHT)));
            bulletVertices.add(new Vertex(quad[3], new Vector2f(0,BULLET_HEIGHT)));
        }
    }

    private void buildHoles(List<BulletHoleState> holes, float dt) {

        List<BulletHoleState> toRemove = new ArrayList<>();

        for (BulletHoleState hole : holes) {

            hole.lifetime -= dt;

            if (hole.lifetime <= 0) {
                toRemove.add(hole);
                continue;
            }

            float size = 20f;
            float x = hole.x - size/2f;
            float y = hole.y - size/2f;

            holeVertices.add(new Vertex(new Vector2f(x,y), new Vector2f(0,0)));
            holeVertices.add(new Vertex(new Vector2f(x+size,y), new Vector2f(size,0)));
            holeVertices.add(new Vertex(new Vector2f(x+size,y+size), new Vector2f(size,size)));
            holeVertices.add(new Vertex(new Vector2f(x,y+size), new Vector2f(0,size)));
        }

        holes.removeAll(toRemove);
    }
}