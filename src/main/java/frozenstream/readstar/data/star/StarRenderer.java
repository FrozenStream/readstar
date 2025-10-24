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

public class StarRenderer {
    private static boolean bufferBuilt = false;
    private static final VertexBuffer starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private static final VertexBuffer starlightBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    private static final VertexBuffer TempBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    private static final ResourceLocation STAR_LOCATION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/stars.png");
    private static final ResourceLocation STARLIGHT_LOCATION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/environment/starlight.png");

    private static final Vector3f OriVec = new Vector3f(0.0F, 0.0F, -1.0F);
    private static final float defaultSize = 0.749f;

    private static final Quaternionf quaternionf = new Quaternionf();
    private static final Vector3f positionVec = new Vector3f();
    private static float Rz;

    private static float Vmag;

    private static final Vector2f[] uvs = new Vector2f[4];
    private static final Vector3f[] v = new Vector3f[4];

    private static final Star[] BrightStars = new Star[1024];
    private static int brightStarCount;

    private static int uvWidth, uvHeight;

    static {
        uvs[0] = new Vector2f();
        uvs[1] = new Vector2f();
        uvs[2] = new Vector2f();
        uvs[3] = new Vector2f();

        v[0] = new Vector3f();
        v[1] = new Vector3f();
        v[2] = new Vector3f();
        v[3] = new Vector3f();
    }

    public static void init() {
        uvWidth = 4;
        uvHeight = 3;
        bufferBuilt = false;
    }


    public static void buildStarsBuffer() {
        starsBuffer.bind();
        starsBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();

        starlightBuffer.bind();
        starlightBuffer.upload(drawStarlight(Tesselator.getInstance()));
        VertexBuffer.unbind();
        bufferBuilt = true;
    }

    private static MeshData buildMash(BufferBuilder bufferbuilder, Star[] stars, int count, float size, int VmagOffset) {
        RandomSource randomsource = RandomSource.create(10842L);
        for (int i = 0; i < count; i++) {
            stars[i].position().normalize(100.0F, positionVec);
            // 顶点到星星位置并附加随机旋转
            Rz = randomsource.nextFloat() * util.PI * 2.0f;
            quaternionf.identity().rotateTo(OriVec, positionVec).rotateZ(Rz);
            v[0].set(size, -size, 0.0F).rotate(quaternionf);
            v[1].set(size, size, 0.0F).rotate(quaternionf);
            v[2].set(-size, size, 0.0F).rotate(quaternionf);
            v[3].set(-size, -size, 0.0F).rotate(quaternionf);

            Textures.getUV(uvWidth, uvHeight, stars[i].type(), uvs);

            Vmag = StarManager.getAlphaFromVmag(stars[i].Vmag() + VmagOffset);

            bufferbuilder.addVertex(v[0].add(positionVec)).setUv(uvs[0].x, uvs[0].y).setColor(1, 1, 1, Vmag);
            bufferbuilder.addVertex(v[1].add(positionVec)).setUv(uvs[1].x, uvs[1].y).setColor(1, 1, 1, Vmag);
            bufferbuilder.addVertex(v[2].add(positionVec)).setUv(uvs[2].x, uvs[2].y).setColor(1, 1, 1, Vmag);
            bufferbuilder.addVertex(v[3].add(positionVec)).setUv(uvs[3].x, uvs[3].y).setColor(1, 1, 1, Vmag);

        }
        return bufferbuilder.buildOrThrow();

    }

    private static MeshData drawStars(Tesselator tesselator) {
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        return buildMash(bufferbuilder, StarManager.stars, StarManager.starCount,
                defaultSize, 0);
    }

    private static MeshData drawStarlight(Tesselator tesselator) {
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        brightStarCount = 0;
        for (int i = 0; i < StarManager.starCount; i++) {
            if (StarManager.stars[i].Vmag() > 1) continue;
            BrightStars[brightStarCount] = StarManager.stars[i];
            brightStarCount++;
        }
        return buildMash(bufferbuilder, BrightStars, brightStarCount,
                defaultSize * 5, 4);
    }

    public static void RenderStars(Matrix4f viewMatrix, Matrix4f projectionMatrix, float light) {
        if (light > 0.0F && bufferBuilt) {
            RenderSystem.setShaderColor(1, 1, 1, light);
            RenderSystem.setShaderTexture(0, STAR_LOCATION);
            starsBuffer.bind();
            starsBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexColorShader());
            VertexBuffer.unbind();

            RenderSystem.setShaderTexture(0, STARLIGHT_LOCATION);
            starlightBuffer.bind();
            starlightBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexColorShader());
            VertexBuffer.unbind();
        }
    }

    public static void RenderNearStars(Matrix4f viewMatrix, Matrix4f projectionMatrix, Vector3f eye, float nearDistance, float scaling, float light) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1, 1, 1, light);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder;
        MeshData meshData;

        ArrayList<Star> near = StarManager.lookingNear(eye, nearDistance);
        if (near.isEmpty()) return;

        bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, STAR_LOCATION);
        meshData = buildMash(bufferbuilder, near.toArray(new Star[0]), near.size(),
                defaultSize * 1.5f * scaling, 0);
        TempBuffer.bind();
        TempBuffer.upload(meshData);
        TempBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexColorShader());
        VertexBuffer.unbind();

        brightStarCount = 0;
        for (Star star : near)
            if (star.Vmag() < 1) {
                BrightStars[brightStarCount] = star;
                brightStarCount++;
            }
        if (brightStarCount == 0) return;

        bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, STARLIGHT_LOCATION);
        meshData = buildMash(bufferbuilder, BrightStars, brightStarCount,
                defaultSize * 1.5f * scaling * 5, 4);
        TempBuffer.bind();
        TempBuffer.upload(meshData);
        TempBuffer.drawWithShader(viewMatrix, projectionMatrix, GameRenderer.getPositionTexColorShader());
        VertexBuffer.unbind();
    }
}
