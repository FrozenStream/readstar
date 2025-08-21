package frozenstream.readstar.data.star;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import frozenstream.readstar.data.Textures;
import frozenstream.readstar.util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class StarRenderer {
    private static boolean bufferBuilt = false;
    private static final VertexBuffer starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    private static final ResourceLocation STAR_LOCATION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/stars.png");

    private static final Vector3f OriVec = new Vector3f(0.0F, 0.0F, -1.0F);
    private static final float defaultSize = 0.486f;

    private static final Quaternionf quaternionf = new Quaternionf();
    private static final Vector3f positionVec = new Vector3f();
    private static float Rz;
    private static float s;

    private static final Vector2f[] uvs = new Vector2f[4];
    private static final Vector3f[] v = new Vector3f[4];

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


    public static void buildStarsBuffer() {
        starsBuffer.bind();
        starsBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
        bufferBuilt = true;
    }

    private static MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        s = defaultSize;
        for (int i = 0; i < StarManager.starCount; i++) {
            StarManager.stars[i].position().normalize(100.0F, positionVec);
            // 顶点到星星位置并附加随机旋转
            Rz = randomsource.nextFloat() * util.PI * 2.0f;
            quaternionf.identity().rotateTo(OriVec, positionVec).rotateZ(Rz);
            v[0].set(s, -s, 0.0F).rotate(quaternionf);
            v[1].set(s, s, 0.0F).rotate(quaternionf);
            v[2].set(-s, s, 0.0F).rotate(quaternionf);
            v[3].set(-s, -s, 0.0F).rotate(quaternionf);

            Textures.getp(4, 3, StarManager.stars[i].type(), uvs);

            bufferbuilder.addVertex(v[0].add(positionVec)).setUv(uvs[0].x, uvs[0].y);
            bufferbuilder.addVertex(v[1].add(positionVec)).setUv(uvs[1].x, uvs[1].y);
            bufferbuilder.addVertex(v[2].add(positionVec)).setUv(uvs[2].x, uvs[2].y);
            bufferbuilder.addVertex(v[3].add(positionVec)).setUv(uvs[3].x, uvs[3].y);

        }
        return bufferbuilder.buildOrThrow();
    }

    public static void RenderStars(Matrix4f viewMatrix, Matrix4f projectionMatrix, float light) {
        if (light > 0.0F && bufferBuilt) {
            RenderSystem.setShaderColor(1, 1, 1, light);
            RenderSystem.setShaderTexture(0, STAR_LOCATION);
            starsBuffer.bind();
            starsBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexShader());
            VertexBuffer.unbind();
        }
    }

    public static void RenderNearStars(PoseStack.Pose pose, Vector3f eye, float nearDistance, float scaling, float light) {
        RandomSource randomsource = RandomSource.create(10842L);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, light);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.setShaderTexture(0, STAR_LOCATION);

        ArrayList<Star> near = StarManager.lookingNear(eye, nearDistance);
        if (near.isEmpty()) return;

        s = defaultSize * 2.0f * scaling;
        for (Star star : near) {
            star.position().normalize(100.0f, positionVec);

            Rz = randomsource.nextFloat() * util.PI * 2.0f;
            quaternionf.identity().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), positionVec).rotateZ(Rz);

            v[0].set(s, -s, 0.0F).rotate(quaternionf);
            v[1].set(s, s, 0.0F).rotate(quaternionf);
            v[2].set(-s, s, 0.0F).rotate(quaternionf);
            v[3].set(-s, -s, 0.0F).rotate(quaternionf);

            Textures.getp(4, 3, star.type(), uvs);

            bufferbuilder.addVertex(pose, v[0].add(positionVec)).setUv(uvs[0].x, uvs[0].y);
            bufferbuilder.addVertex(pose, v[1].add(positionVec)).setUv(uvs[1].x, uvs[1].y);
            bufferbuilder.addVertex(pose, v[2].add(positionVec)).setUv(uvs[2].x, uvs[2].y);
            bufferbuilder.addVertex(pose, v[3].add(positionVec)).setUv(uvs[3].x, uvs[3].y);
        }
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
