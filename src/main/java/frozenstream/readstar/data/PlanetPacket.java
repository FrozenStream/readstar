package frozenstream.readstar.data;

import net.minecraft.world.phys.Vec3;

/**
 * @param name          星体名称
 * @param description   星体描述
 * @param parent      环绕星体 【特殊值】 "Centre":无环绕（居中）  "None":无环绕（天球）
 * @param mass          星体质量
 * @param axis          星体轴向向量，以XY轴旋转量描述
 * @param a             半长轴
 * @param e             偏心率
 * @param i             轨道倾角（单位：弧度）
 * @param w             近心点幅角（单位：弧度）
 * @param o             升交点经度（单位：弧度）
 * @param M0            初始平近点角（单位：弧度）
 * 若轨道为天球，取平近点角M始终为M0
 */
public record PlanetPacket(
        String name,
        String description,
        String parent,
        double mass,
        Vec3 axis,
        double a,
        double e,
        double i,
        double w,
        double o,
        double M0
) {

}
