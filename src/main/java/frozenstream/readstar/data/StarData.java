package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * @param name          星体名称
 * @param description   星体描述
 * @param orbiting      环绕星体 【特殊值】 "Centre":无环绕（居中）  "None":无环绕（天球）
 * @param mass          星体质量
 * @param axis          星体轴向向量，以XY轴旋转量描述
 * 轨道六要素：计算行星在惯性系中的三维位置，若轨道不存在，则以下参数无效
 * @param a             半长轴
 * @param e             偏心率
 * @param i             轨道倾角（单位：弧度）
 * @param w             近心点幅角（单位：弧度）
 * @param o             升交点经度（单位：弧度）
 * @param M0            初始平近点角（单位：弧度）
 * 若轨道为天球，取平近点角M始终为M0
 */
public record StarData(
        String name,
        String description,
        String orbiting,
        double mass,
        ArrayList<Double> axis,
        double a,
        double e,
        double i,
        double w,
        double o,
        double M0
) {
    private static final double G = 6.67430e-11;

    /**
     * 获取 平近点角的角速度
     * @param Mass  环绕星体的质量
     * @param a     轨道半长轴
     */
    private static double mean_anomaly_angular_velocity(double Mass, double a){
        return Math.sqrt(G *  Mass / Math.pow(a, 3));
    }

    /**
     * 使用牛顿迭代法解 Kepler 方程：M = E - e * sin(E)
     * @param M     平近点角
     * @param e     轨道偏心率
     */
    public static double solveKepler(double M, double e) {
        double E;
        if (e < 0.8) E = M; // 初始猜测
        else E = Math.PI; // 高偏心率时使用 π

        for (int i = 0; i < 100; i++) {
            double f = E - e * Math.sin(E) - M;
            double df = 1 - e * Math.cos(E);
            double delta = f / df;
            E -= delta;
            if (Math.abs(delta) < 1e-5) break;
        }
        return E;
    }

    /**
     * 计算星体的位置
     * @param Mass  父星星体质量
     * @param t     时间（秒）
     * @return      星体的 XYZ 坐标
     */
    public Vec3 calPosition(double Mass, double t) {
        if (orbiting.equals("None")) t = 0;
        double M = M0 + mean_anomaly_angular_velocity(Mass, a) * t;
        M = M % (2 * Math.PI); // 归一化到 [0, 2π)

        // 步骤3：解 Kepler 方程，求偏近点角 E
        double E = solveKepler(M, e);

        // 步骤4：计算轨道平面坐标 (xp, yp)
        double xp = a * (Math.cos(E) - e);
        double yp = a * Math.sqrt(1 - e * e) * Math.sin(E);

        // 步骤5：构造旋转矩阵并计算 XYZ
        double cos_Omega = Math.cos(o);
        double sin_Omega = Math.sin(o);
        double cos_i = Math.cos(i);
        double sin_i = Math.sin(i);
        double cos_w = Math.cos(w);
        double sin_w = Math.sin(w);

        double X = (cos_Omega * cos_w - sin_Omega * sin_w * cos_i) * xp
                + (-cos_Omega * sin_w - sin_Omega * cos_w * cos_i) * yp;

        double Y = (sin_Omega * cos_w + cos_Omega * sin_w * cos_i) * xp
                + (-sin_Omega * sin_w + cos_Omega * cos_w * cos_i) * yp;

        double Z = (sin_w * sin_i) * xp + (cos_w * sin_i) * yp;

        return new Vec3(X, Y, Z);
    }
}
