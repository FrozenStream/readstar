package frozenstream.readstar.data.star;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.data.planet.PlanetManager;
import frozenstream.readstar.util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class StarManager {
    public static Star[] stars = new Star[32768];
    public static int starCount = 0;

    private static final Vector3f OriginX = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f OriginY = new Vector3f(0.0F, 1.0F, 0.0F);

    public static void init() {
        stars = StarResourceReader.getStars().toArray(new Star[0]);
        starCount = StarResourceReader.getStars().size();
        StarRenderer.init();
        if (starCount > 0) StarRenderer.buildStarsBuffer();
        Constants.LOG.info("Star Manager: INIT!");
    }


    public static Matrix4f observeFrom(Planet planet, long t) {
        Matrix4f mat = new Matrix4f();
        Vector3f axis = planet.axis;
        Vector3f current = PlanetManager.updateCurrentSkyVec(planet, t);

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


    /**
     * 将星等(Vmag)映射到0-1的透明度值
     * 使用天文星等公式: m1 - m2 = -2.5 * log10(I1/I2)
     * 其中较亮的星星星等数值更小
     *
     * @param Vmag 视星等
     * @param maxVisibleMagnitude 最大可见星等（默认为6等星，人眼极限）
     * @return 透明度值，范围0-1
     */
    public static float getAlphaFromVmag(float Vmag, float maxVisibleMagnitude) {
        // 对于超过最大可见星等的星体，透明度为0
        if (Vmag > maxVisibleMagnitude) return 0.05f;
        else if(Vmag < 0) return 1f;

        // 计算相对亮度比例
        // 公式: I1/I2 = 10^((m2-m1)/2.5)
        final float benchmarkVmag = 0f;
        float brightnessRatio = (float) Math.pow(2, (benchmarkVmag - Vmag) / 2.5);
        float alpha = Math.min(1.0f, brightnessRatio);
        return Math.max(0.1f, alpha);
    }

    /**
     * 将星等(Vmag)映射到0-1的透明度值（使用默认最大可见星等6）
     *
     * @param Vmag 视星等
     * @return 透明度值，范围0-1
     */
    public static float getAlphaFromVmag(float Vmag) {
        return getAlphaFromVmag(Vmag, 7.0f);
    }





}
