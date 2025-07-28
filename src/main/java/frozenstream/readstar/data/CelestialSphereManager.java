package frozenstream.readstar.data;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CelestialSphereManager {
    // 基于中心恒星坐标系的单位向量
    private static Vector3f[] stars = new Vector3f[32768];

    private static final Vector3f OriginY = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Quaternionf rotateTo(Vector3f normalAxis){
        Quaternionf q = new Quaternionf();
        return q.rotateTo(OriginY, normalAxis);
    }

    public VertexBuffer getStarsBuffer(Quaternionf quat){
        VertexBuffer starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        starBuffer.bind();
        starBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
        return starBuffer;
    }

    private MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for(int j = 0; j < 1500; ++j) {
            float f1 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f2 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f3 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f4 = 0.15F + randomsource.nextFloat() * 0.1F;
            float f5 = Mth.lengthSquared(f1, f2, f3);
            if (!(f5 <= 0.010000001F) && !(f5 >= 1.0F)) {
                Vector3f vector3f = (new Vector3f(f1, f2, f3)).normalize(100.0F);
                float f6 = (float)(randomsource.nextDouble() * 3.1415927410125732 * 2.0);
                Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(f6);
                bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, -f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, -f4, 0.0F)).rotate(quaternionf)));
            }
        }
        return bufferbuilder.buildOrThrow();
    }
}
