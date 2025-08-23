package frozenstream.readstar.data.star;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class StarManager {
    public static final Star[] stars = new Star[32768];
    public static int starCount = 0;

    private static final Vector3f OriginX = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f OriginY = new Vector3f(0.0F, 1.0F, 0.0F);

    public static void init() {
        starCount = 0;
        StarRenderer.init();
    }


    public static void register(String name, Vector3f position, int type) {
        stars[starCount] = new Star(name, position.normalize(), type);
        starCount++;
    }

    public static void register(Star star) {
        stars[starCount] = star;
        starCount++;
    }


    public static void Display_Build() {
        for(int i = 0; i < starCount; i++){
            Constants.LOG.info("StarManager: Load {} TYPE:{}", stars[i].name(), stars[i].type());
        }
        StarRenderer.buildStarsBuffer();
    }



    public static Matrix4f observeFrom(Planet planet, long t) {
        Matrix4f mat = new Matrix4f();
        Vector3f axis = planet.axis;
        Vector3f current = planet.updateCurrentSkyVec(t);

        Quaternionf rotationToY = (new Quaternionf()).rotationTo(axis, OriginY);
        Vector3f rotatedCurrent = rotationToY.transform(current, new Vector3f());

        Quaternionf rotationToX = (new Quaternionf()).rotationTo(rotatedCurrent, OriginX);

        // 创建表示120度绕(1,1,1)轴旋转的四元数
        Quaternionf rotation = new Quaternionf().rotateAxis(120f / 180.0f * util.PI, 1.0f, 1.0f, 1.0f);

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
