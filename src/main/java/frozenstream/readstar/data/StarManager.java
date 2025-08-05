package frozenstream.readstar.data;

import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class StarManager {
    private static final float PI = 3.1415927410125732f;
    private static final Star[] stars = new Star[32768];
    private static int starCount = 0;

    public static final VertexBuffer starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

    public static final ResourceLocation STAR_LOCATION = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/star.png");

    private static final Vector3f OriginX = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f OriginY = new Vector3f(0.0F, 1.0F, 0.0F);


    public static void init(List<StarPacket> starList) {
        for (StarPacket star : starList) {
            stars[starCount] = new Star(star.name(), star.description(), star.position().toVector3f().normalize(), star.type());
            starCount++;
        }
        buildStarsBuffer();
    }

    public static void buildStarsBuffer() {
        if (starCount == 0) return;
        starsBuffer.bind();
        starsBuffer.upload(drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }

    private static MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = 1f;
        Vector3f[] v = new Vector3f[4];

        for (int i = 0; i < starCount; i++) {
            Vector3f vector3f = stars[i].position().normalize(100.0F, new Vector3f());
            float Rz = randomsource.nextFloat() * PI * 2.0f;
            Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(Rz);
            v[0] = (new Vector3f(s, -s, 0.0F)).rotate(quaternionf);
            v[1] = (new Vector3f(s, s, 0.0F)).rotate(quaternionf);
            v[2] = (new Vector3f(-s, s, 0.0F)).rotate(quaternionf);
            v[3] = (new Vector3f(-s, -s, 0.0F)).rotate(quaternionf);

            Vector2f[] uvs = Textures.getp(4, 1, stars[i].type());

            bufferbuilder.addVertex(vector3f.add(v[0], new Vector3f())).setUv(uvs[0].x, uvs[0].y);
            bufferbuilder.addVertex(vector3f.add(v[1], new Vector3f())).setUv(uvs[1].x, uvs[1].y);
            bufferbuilder.addVertex(vector3f.add(v[2], new Vector3f())).setUv(uvs[2].x, uvs[2].y);
            bufferbuilder.addVertex(vector3f.add(v[3], new Vector3f())).setUv(uvs[3].x, uvs[3].y);

        }
        return bufferbuilder.buildOrThrow();
    }

    public static void RenderStars(Matrix4f projectionMatrix, Matrix4f viewMatrix) {

    }

    public static Matrix4f observeFrom(Planet planet, long t) {
        Matrix4f mat = new Matrix4f();
        Vector3f axis = planet.axis;
        Vector3f current = planet.updateCurrentSkyVec(t);

        Quaternionf rotationToY = (new Quaternionf()).rotationTo(axis, OriginY);
        Vector3f rotatedCurrent = rotationToY.transform(current, new Vector3f());

        Quaternionf rotationToX = (new Quaternionf()).rotationTo(rotatedCurrent, OriginX);

        // 创建表示120度绕(1,1,1)轴旋转的四元数
        Quaternionf rotation = new Quaternionf().rotateAxis(120f / 180.0f * PI, 1.0f, 1.0f, 1.0f);

        // 组合旋转
        Quaternionf finalRotation = new Quaternionf();
        finalRotation.set(rotation);
        finalRotation.mul(rotationToX);
        finalRotation.mul(rotationToY);

        return mat.rotation(finalRotation);
    }


    public static Star lookingAt(Vector3f eye, float nearDistance){
        float minDistance = nearDistance;
        Star closestStar = null;
        for (int i = 0; i < starCount; i++) {
            Vector3f starPos = stars[i].position();
            float distance = eye.distance(starPos);
            if (distance < minDistance) {
                minDistance = distance;
                closestStar = stars[i];
            }
        }
        return closestStar;
    }


    public static ArrayList<Star> lookingNear(Vector3f eye, float nearDistance){
        ArrayList<Star> closestStars = new ArrayList<>();
        for (int i = 0; i < starCount; i++) {
            Vector3f starPos = stars[i].position();
            float distance = eye.distance(starPos);
            if (distance < nearDistance) {
                closestStars.add(stars[i]);
            }
        }
        return closestStars;
    }





}
