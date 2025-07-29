package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class PlanetManager {
    public static final float PI = 3.14159265358979f;
    private static boolean star_prepared = false;
    private static int size = 0;
    private static final Map<String, Planet> name_map = new TreeMap<>();

    public static void init(List<StarData> starList) {
        size = starList.size();
        for(StarData star : starList){
            String name = star.name();
            Oribit oribit = new Oribit(star.a(), star.e(), star.i(), star.w(),star.o(), star.M0());
            String parent_name = star.parent();
            if(!name_map.containsKey(name)) name_map.put(name, new Planet());
            Planet parent = Planet.VOID;
            if(!name_map.containsKey(parent_name) && !parent_name.equals("Centre")) {
                name_map.put(parent_name, new Planet());
                parent = name_map.get(parent_name);
            }
            Planet planet = new Planet(name, star.description(), star.mass(), star.axis().toVector3f(), oribit, parent);
            name_map.get(name).copy(planet);
        }
        checkAndDisplay();
    }

    public static Planet getPlanet(String name) {
        return name_map.get(name);
    }

    private static void checkAndDisplay() {
        boolean flag = false;
        for (Planet planet : name_map.values())
            if (planet.mass == 0) {
                flag = true;
                break;
            }
        if (flag) Constants.LOG.error("数据错误！请检查行星树是否完整！");
        for (Planet planet : name_map.values())
            Constants.LOG.info("PlanetManager: {} have parent {}", planet.name, planet.parent.name);
    }


    public static void updatePositions(double t) {
        for (Planet planet : name_map.values()) planet.pos_updated = false;
        for (Planet planet : name_map.values())
            if (!planet.pos_updated) planet.updatePosition(t);
        if (size > 0) star_prepared = true;
    }

    public static ArrayList<StarDataInSky> getInSky(String name) {
        if (!star_prepared) {
            Constants.LOG.warn("数据未准备好，请稍后再试...");
            return new ArrayList<>();
        }
        ArrayList<StarDataInSky> pos = new ArrayList<>();
        Planet mainP = name_map.get(name);

        long daytime = 0;
        if (Minecraft.getInstance().level != null) daytime = Minecraft.getInstance().level.getDayTime();

        Vector3f cur_vec = mainP.updateCurrentSkyVec(daytime);

        // 计算叉积以获得x轴方向
        Vector3f y_axis = cur_vec.normalize();
        Vector3f z_axis = new Vector3f(mainP.axis);
        Vector3f x_axis = new Vector3f();
        z_axis.cross(cur_vec, x_axis).normalize();

        for(Planet planet : name_map.values()) {
            if (planet == mainP) continue;
            // 应用矩阵变换以获得局部坐标系中的坐标
            Vector3f rel_pos = new Vector3f();
            planet.position.sub(mainP.position, rel_pos);
            float x = rel_pos.dot(x_axis);
            float y = rel_pos.dot(y_axis);
            float z = rel_pos.dot(z_axis);

            pos.add(new StarDataInSky(x, y, z, false,null));
        }
        return pos;
    }
}
