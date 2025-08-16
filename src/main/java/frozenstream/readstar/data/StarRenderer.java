package frozenstream.readstar.data;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class StarRenderer {
    public static final VertexBuffer starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    public static void buildStarsBuffer() {
        if (StarManager.starCount == 0) return;
        starsBuffer.bind();
        starsBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }

    private static MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = 1.024f;
        Vector3f[] v = new Vector3f[4];

        for (int i = 0; i < StarManager.starCount; i++) {
            Vector3f vector3f = StarManager.stars[i].position().normalize(100.0F, new Vector3f());
            float Rz = randomsource.nextFloat() * util.PI * 2.0f;
            Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(Rz);
            v[0] = (new Vector3f(s, -s, 0.0F)).rotate(quaternionf);
            v[1] = (new Vector3f(s, s, 0.0F)).rotate(quaternionf);
            v[2] = (new Vector3f(-s, s, 0.0F)).rotate(quaternionf);
            v[3] = (new Vector3f(-s, -s, 0.0F)).rotate(quaternionf);

            Vector2f[] uvs = Textures.getp(4, 1, StarManager.stars[i].type());

            bufferbuilder.addVertex(vector3f.add(v[0], new Vector3f())).setUv(uvs[0].x, uvs[0].y);
            bufferbuilder.addVertex(vector3f.add(v[1], new Vector3f())).setUv(uvs[1].x, uvs[1].y);
            bufferbuilder.addVertex(vector3f.add(v[2], new Vector3f())).setUv(uvs[2].x, uvs[2].y);
            bufferbuilder.addVertex(vector3f.add(v[3], new Vector3f())).setUv(uvs[3].x, uvs[3].y);
        }
        return bufferbuilder.buildOrThrow();
    }

    public static void RenderStars(Matrix4f viewMatrix, Matrix4f projectionMatrix, float light) {
        if (light > 0.0F) {
            RenderSystem.setShaderColor(1, 1, 1, light);
            RenderSystem.setShaderTexture(0, StarManager.STAR_LOCATION);
            starsBuffer.bind();
            starsBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexShader());
            VertexBuffer.unbind();
        }
    }

    public static void RenderNearStars(PoseStack.Pose pose, Vector3f eye, float nearDistance, float scaling) {
        RandomSource randomsource = RandomSource.create(10842L);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.setShaderTexture(0, StarManager.STAR_LOCATION);

        ArrayList<Star> near = StarManager.lookingNear(eye, nearDistance);
        if (near.isEmpty()) return;

        for (Star star : near) {
            Vector3f vector3f = star.position().normalize(100.0f, new Vector3f());

            float s = 1.024f * scaling;
            Vector3f[] v = new Vector3f[4];
            float Rz = randomsource.nextFloat() * util.PI * 2.0f;
            Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(Rz);
            v[0] = (new Vector3f(s, -s, 0.0F)).rotate(quaternionf);
            v[1] = (new Vector3f(s, s, 0.0F)).rotate(quaternionf);
            v[2] = (new Vector3f(-s, s, 0.0F)).rotate(quaternionf);
            v[3] = (new Vector3f(-s, -s, 0.0F)).rotate(quaternionf);

            Vector2f[] uvs = Textures.getp(4, 1, star.type());

            bufferbuilder.addVertex(pose, vector3f.add(v[0], new Vector3f())).setUv(uvs[0].x, uvs[0].y);
            bufferbuilder.addVertex(pose, vector3f.add(v[1], new Vector3f())).setUv(uvs[1].x, uvs[1].y);
            bufferbuilder.addVertex(pose, vector3f.add(v[2], new Vector3f())).setUv(uvs[2].x, uvs[2].y);
            bufferbuilder.addVertex(pose, vector3f.add(v[3], new Vector3f())).setUv(uvs[3].x, uvs[3].y);
        }
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
