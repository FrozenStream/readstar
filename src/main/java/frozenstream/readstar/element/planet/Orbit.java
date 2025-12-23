package frozenstream.readstar.element.planet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Orbit(
        double a,
        double e,
        double i,
        double w,
        double o,
        double M0
) {
    private static final double G = 6.67430e-11;

    public static final StreamCodec<RegistryFriendlyByteBuf, Orbit> ORBIT_CODEC =
            StreamCodec.of(
                    (buf, orbit) -> {
                        buf.writeDouble(orbit.a());
                        buf.writeDouble(orbit.e());
                        buf.writeDouble(orbit.i());
                        buf.writeDouble(orbit.w());
                        buf.writeDouble(orbit.o());
                        buf.writeDouble(orbit.M0());
                    },
                    buf -> new Orbit(
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()
                    )
            );

    /**
     * 获取 平近点角的角速度
     *
     * @param Mass 环绕星体的质量
     * @param a    轨道半长轴
     */
    private static double mean_anomaly_angular_velocity(double Mass, double a) {
        return Math.sqrt(G * Mass / Math.pow(a, 3));
    }

    /**
     * 使用牛顿迭代法解 Kepler 方程：M = E - e * sin(E)
     *
     * @param M 平近点角
     * @param e 轨道偏心率
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
     *
     * @param Mass 父星星体质量
     * @param t    时间（秒）
     * @return 星体的 XYZ 坐标
     */
    public Vector3fc calPosition(double Mass, double t) {
        if (a == 0) return new Vector3f(0, 0, 0);

        double M = M0 + mean_anomaly_angular_velocity(Mass, a) * t;
        M = M % (2 * Math.PI); // 归一化到 [0, 2π)

        // 步骤3：解 Kepler 方程，求偏近点角 E
        double E = solveKepler(M, e);

        // 步骤4：计算轨道平面坐标 (xp, yp)
        double xp = a * (Math.cos(E) - e);
        double yp = a * Math.sqrt(1 - e * e) * Math.sin(E);

        // 步骤5：构造旋转矩阵并计算 XYZ
        // i 是轨道倾角，w 是近心点俯角， o 是升交点经度
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

        return new Vector3f((float) X, (float) Y, (float) Z);
    }
}
