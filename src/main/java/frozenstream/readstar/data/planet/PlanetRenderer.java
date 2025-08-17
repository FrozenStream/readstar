package frozenstream.readstar.data.planet;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.data.Textures;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlanetRenderer {

    public static void drawSun(Tesselator tesselator, Planet observer, PoseStack.Pose pose, long t, float rain) {
        if (!PlanetManager.star_prepared) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Planet planet = PlanetManager.SUN;

        Vector3f vec = planet.position.sub(observer.position, new Vector3f()).normalize(100.0f);
        Quaternionf quaternion = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vec);
        RenderSystem.setShaderTexture(0, Textures.getTexture(planet.name));
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = PlanetManager.getApparentSize(observer, planet);

        Vector2f[] uvs = new Vector2f[4];
        uvs[0] = new Vector2f(1, 0);
        uvs[1] = new Vector2f(1, 1);
        uvs[2] = new Vector2f(0, 1);
        uvs[3] = new Vector2f(0, 0);

        Vector3f[] v = new Vector3f[4];
        v[0] = (new Vector3f(s, -s, 0.0F)).rotate(quaternion);
        v[1] = (new Vector3f(s, s, 0.0F)).rotate(quaternion);
        v[2] = (new Vector3f(-s, s, 0.0F)).rotate(quaternion);
        v[3] = (new Vector3f(-s, -s, 0.0F)).rotate(quaternion);

        vec.normalize(101.0f);
        builder.addVertex(pose, v[0].add(vec)).setUv(uvs[3].x, uvs[3].y);
        builder.addVertex(pose, v[1].add(vec)).setUv(uvs[0].x, uvs[0].y);
        builder.addVertex(pose, v[2].add(vec)).setUv(uvs[1].x, uvs[1].y);
        builder.addVertex(pose, v[3].add(vec)).setUv(uvs[2].x, uvs[2].y);

        BufferUploader.drawWithShader(builder.buildOrThrow());
    }


    public static void drawPlanets(Tesselator tesselator, Planet observer, PoseStack.Pose pose, long t, float rain) {
        if (!PlanetManager.star_prepared) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        for (Planet planet : PlanetManager.getPlanets()) {
            if (planet == observer) continue;

            Vector3f vec = planet.position.sub(observer.position, new Vector3f()).normalize(100.0f);
            RenderSystem.setShaderTexture(0, Textures.getTexture(planet.name));
            BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            if (planet == PlanetManager.SUN) continue;

            Vector2f[] uvs = Textures.getp(PlanetManager.getLightPhase(observer, planet));

            float s = PlanetManager.getApparentSize(observer, planet);
            Vector3f[] v = new Vector3f[4];

            Vector3f planet_sun = PlanetManager.SUN.position.sub(planet.position, new Vector3f()).normalize();
            Vector3f vec_n = vec.normalize(new Vector3f());
            Vector3f project = vec_n.mul(planet_sun.dot(vec_n), new Vector3f());
            planet_sun.sub(project).normalize();

            Vector3f ano = planet_sun.cross(vec_n, new Vector3f()).normalize();

            v[0] = planet_sun.sub(ano, new Vector3f()).mul(s);
            v[1] = planet_sun.add(ano, new Vector3f()).mul(s);
            planet_sun.mul(-1);
            v[2] = planet_sun.add(ano, new Vector3f()).mul(s);
            v[3] = planet_sun.sub(ano, new Vector3f()).mul(s);


            builder.addVertex(pose, v[0].add(vec)).setUv(uvs[1].x, uvs[1].y);
            builder.addVertex(pose, v[1].add(vec)).setUv(uvs[2].x, uvs[2].y);
            builder.addVertex(pose, v[2].add(vec)).setUv(uvs[3].x, uvs[3].y);
            builder.addVertex(pose, v[3].add(vec)).setUv(uvs[0].x, uvs[0].y);

            BufferUploader.drawWithShader(builder.buildOrThrow());
        }
    }
}
