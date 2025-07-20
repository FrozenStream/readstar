package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.world.phys.Vec3;

import java.util.*;


public class StarManager {
    // 用于存储读取到的数据
    private static int size = 0;
    private static final StarData[] star = new StarData[32768];
    private static final int[] primary_id = new int[32768];
    private static final Vec3[] star_pos = new Vec3[32768];

    public static StarData getStarData(int id) {
        return star[id];
    }

    public static void updateClientData(List<StarData> newData) {
        Map<String, Integer> star_id_map = new HashMap<>();
        size = newData.size();
        int main_star_id = -1;

        for (int i = 0; i < size; i++) {
            star[i] = newData.get(i);
            if (star[i].orbiting().equals("Centre")) main_star_id = i;
            star_id_map.put(star[i].name(), i);
            Constants.LOG.info(star[i].name()+" orbiting " + star[i].orbiting() + " get id " + star_id_map.get(star[i].name()));
        }
        if (main_star_id == -1) Constants.LOG.error("没有找到主星！");

        for (int i = 0; i < size; i++) {
            String orbiting = star[i].orbiting();
            if (orbiting.equals("Centre")) primary_id[i] = -1;
            else if (orbiting.equals("None")) primary_id[i] = main_star_id;
            else primary_id[i] = star_id_map.get(orbiting);
        }
    }

    public static void showData() {
        Constants.LOG.info("显示数据：");
        for (StarData data : star) {
            if (data == null) continue;
            Constants.LOG.info(data.toString());
        }
    }

    private static void updateStar(int id, double t) {
        if (star_pos[id] != null) return;
        if (primary_id[id] == -1) {
            star_pos[id] = Vec3.ZERO;
            return;
        }
        if (star_pos[primary_id[id]] == null) updateStar(primary_id[id], t);
        star_pos[id] = star_pos[primary_id[id]].add(star[id].calPosition(star[primary_id[id]].mass(), t));
    }

    public static void updateStars(double t) {
        for (int i = 0; i < size; i++) star_pos[i] = null;
        for (int i = 0; i < size; i++) {
            updateStar(i, t);
        }
        for (int i = 0; i < size; i++) Constants.LOG.info("星体 {} 的位置为: {}", star[i].name(), star_pos[i]);
    }

    /**
     * 将向量绕给定的轴旋转指定角度
     *
     * @param vector 要旋转的向量
     * @param axis   旋转轴 (必须是单位向量)
     * @param theta  旋转角度 (单位: 弧度)
     * @return 旋转后的向量
     */
    public static Vec3 rotateVectorAroundAxis(Vec3 vector, Vec3 axis, double theta) {
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        // 罗德里格斯公式
        Vec3 term1 = vector.scale(cosTheta);
        Vec3 term2 = axis.cross(vector).scale(sinTheta);

        return term1.add(term2);
    }

    public static ArrayList<Vec3> getStarInSky(int id, double t) {
        Vec3 axis = new Vec3(star[id].axis().get(0), star[id].axis().get(1), star[id].axis().get(2));
        if(axis.length() == 0)axis = new Vec3(0, 1, 0);
        else axis = axis.normalize();
        String orbiting = star[id].orbiting();

        ArrayList<Vec3> insky = new ArrayList<Vec3>();

        if (orbiting.equals("Centre") || orbiting.equals("None")) {
            Vec3 prim_vec = new Vec3(1,0,0);
            double n = axis.dot(prim_vec);
            Vec3 cur_vec = prim_vec.subtract(axis.scale(n));

            // 计算叉积以获得x轴方向
            Vec3 x_axis = axis.cross(cur_vec).normalize();
            Vec3 y_axis = cur_vec.normalize();
            Vec3 z_axis = axis.normalize();

            // 应用矩阵变换以获得局部坐标系中的坐标
            double x = star_pos[id].dot(x_axis);
            double y = star_pos[id].dot(y_axis);
            double z = star_pos[id].dot(z_axis);

            insky.add(new Vec3(x, y, z));
        }
        else {
            Vec3 prim_vec = star_pos[primary_id[id]].subtract(star_pos[id]);
            double n = axis.dot(prim_vec);
            Vec3 noon_vec = prim_vec.subtract(axis.scale(n));

            double theta = (t-6000)/12000*Math.PI;
            Vec3 cur_vec = rotateVectorAroundAxis(noon_vec, axis, theta);

            // 计算叉积以获得x轴方向
            Vec3 x_axis = axis.cross(cur_vec).normalize();
            Vec3 y_axis = cur_vec.normalize();
            Vec3 z_axis = axis.normalize();

            // 应用矩阵变换以获得局部坐标系中的坐标
            double x = star_pos[id].dot(x_axis);
            double y = star_pos[id].dot(y_axis);
            double z = star_pos[id].dot(z_axis);

            insky.add(new Vec3(x, y, z));
        }
        return insky;
    }
}
