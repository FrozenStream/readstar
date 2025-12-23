package frozenstream.readstar.element.planet;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.element.Textures;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlanetRenderer {
    private static final Vector3f OriVec = new Vector3f(0.0F, 0.0F, 1.0F);

    private static final Quaternionf quaternionf = new Quaternionf();
    private static final Vector3f positionVec = new Vector3f();
    private static float s = 0.768f;

    private static final Vector2f[] uvs = new Vector2f[4];
    private static final Vector3f[] v = new Vector3f[4];

    private static final Vector3f p2s = new Vector3f();
    private static final Vector3f o2p = new Vector3f();
    private static final Vector3f project = new Vector3f();
    private static final Vector3f cross = new Vector3f();

    static  {
        uvs[0] = new Vector2f();
        uvs[1] = new Vector2f();
        uvs[2] = new Vector2f();
        uvs[3] = new Vector2f();

        v[0] = new Vector3f();
        v[1] = new Vector3f();
        v[2] = new Vector3f();
        v[3] = new Vector3f();
    }

    public static void drawSun(Tesselator tesselator, Planet observer, PoseStack.Pose pose, float rain) {
        // 设置透明度，下雨则关闭
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rain);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        for(Planet planet : PlanetManager.getPlanets()) {
            if(PlanetManager.getPlanetsLevel(planet) != 1)continue;
            RenderSystem.setShaderTexture(0, planet.getTexture());

            planet.position.sub(observer.position, positionVec).normalize(101.0f);
            quaternionf.identity().rotateTo(OriVec, positionVec);
            BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            s = PlanetManager.getApparentSize(observer, planet);

            uvs[0].set(1, 0);
            uvs[1].set(1, 1);
            uvs[2].set(0, 1);
            uvs[3].set(0, 0);

            v[0].set(s, -s, 0.0F).rotate(quaternionf);
            v[1].set(-s, -s, 0.0F).rotate(quaternionf);
            v[2].set(-s, s, 0.0F).rotate(quaternionf);
            v[3].set(s, s, 0.0F).rotate(quaternionf);

            builder.addVertex(pose, v[0].add(positionVec)).setUv(uvs[3].x, uvs[3].y);
            builder.addVertex(pose, v[1].add(positionVec)).setUv(uvs[0].x, uvs[0].y);
            builder.addVertex(pose, v[2].add(positionVec)).setUv(uvs[1].x, uvs[1].y);
            builder.addVertex(pose, v[3].add(positionVec)).setUv(uvs[2].x, uvs[2].y);

            BufferUploader.drawWithShader(builder.buildOrThrow());
        }
    }


    public static void drawPlanets(Tesselator tesselator, Planet observer, PoseStack.Pose pose, float rain, float starLight) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        for (Planet planet : PlanetManager.getPlanets()) {
            if (planet == observer) continue;
            if (PlanetManager.isSunOrRoot(planet)) continue;

            Planet sun = PlanetManager.whichIsYourSun(planet);
            // 设置透明度，下雨或离太阳过近
            float min = starLight+0.3f;
            min = Math.min(min, rain);
            float covered = PlanetManager.getCoveredBySun(observer, planet);
            min = Math.min(min, covered);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, min);

            planet.position.sub(observer.position, positionVec).normalize(100.0f);
            RenderSystem.setShaderTexture(0, planet.getTexture());
            BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            Textures.getUV(PlanetManager.getLightPhase(observer, planet), uvs);

            s = PlanetManager.getApparentSize(observer, planet);

            sun.position.sub(planet.position, p2s).normalize();
            positionVec.normalize(o2p);
            o2p.mul(p2s.dot(o2p), project);
            p2s.sub(project).normalize();
            p2s.cross(o2p, cross).normalize();

            p2s.sub(cross, v[0]).mul(s);
            p2s.add(cross, v[1]).mul(s);
            p2s.mul(-1);
            p2s.add(cross, v[2]).mul(s);
            p2s.sub(cross, v[3]).mul(s);


            builder.addVertex(pose, v[0].add(positionVec)).setUv(uvs[1].x, uvs[1].y);
            builder.addVertex(pose, v[1].add(positionVec)).setUv(uvs[2].x, uvs[2].y);
            builder.addVertex(pose, v[2].add(positionVec)).setUv(uvs[3].x, uvs[3].y);
            builder.addVertex(pose, v[3].add(positionVec)).setUv(uvs[0].x, uvs[0].y);

            BufferUploader.drawWithShader(builder.buildOrThrow());
        }
    }
}
