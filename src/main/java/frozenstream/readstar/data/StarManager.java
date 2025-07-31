package frozenstream.readstar.data;

import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class StarManager {
    private static final Star[] stars = new Star[32768];
    private static int starCount = 0;

    public static final VertexBuffer starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    private static final Vector3f OriginX = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f OriginY = new Vector3f(0.0F, 1.0F, 0.0F);

    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");


    public static void init(List<StarPacket> starList) {
        for (StarPacket star : starList) {
            stars[starCount] = new Star(star.name(), star.description(), star.position().toVector3f());
            starCount++;
        }
        buildStarsBuffer();
    }

    public static void buildStarsBuffer() {
        starsBuffer.bind();
        starsBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }
    private static MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        float f4 = 10;
        Vector3f vector3f = (new Vector3f(1, 0, 0)).normalize(100.0F);
        float f6 = (float)(randomsource.nextDouble() * 3.1415927410125732 * 2.0);
        Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(f6);
        bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, -f4, 0.0F)).rotate(quaternionf)));
        bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, f4, 0.0F)).rotate(quaternionf)));
        bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, f4, 0.0F)).rotate(quaternionf)));
        bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, -f4, 0.0F)).rotate(quaternionf)));

        return bufferbuilder.buildOrThrow();
    }

    public static Matrix4f observeFrom(Planet planet, long t){
        Matrix4f mat = new Matrix4f();

        // 获取行星的轴向和当前天空向量
        Vector3f axis = planet.axis;
        Vector3f current = planet.updateCurrentSkyVec(t);

        // 创建一个将axis向量旋转到Y轴的四元数
        Quaternionf rotationToY = new Quaternionf();
        rotationToY.rotationTo(axis, OriginY);

        // 应用第一步旋转到current向量
        Vector3f rotatedCurrent = new Vector3f(current);
        rotationToY.transform(rotatedCurrent);

        // 计算将rotatedCurrent旋转到X轴的四元数
        Quaternionf rotationToX = new Quaternionf();
        rotationToX.rotationTo(rotatedCurrent, OriginX);

        // 组合两个旋转
        Quaternionf finalRotation = new Quaternionf();
        finalRotation.set(rotationToX);
        finalRotation.mul(rotationToY);

        // 将四元数转换为矩阵
        return mat.rotation(finalRotation);
    }
}
