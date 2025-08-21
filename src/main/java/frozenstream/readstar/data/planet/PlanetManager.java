package frozenstream.readstar.data.planet;

import frozenstream.readstar.Constants;
import frozenstream.readstar.util;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.*;

//TODO: 添加星球自转周期

public class PlanetManager {
    public static boolean star_prepared = false;
    private static int size = 0;
    private static final Map<String, Planet> name_map = new TreeMap<>();

    public static Planet SUN = null;


    public static void register(String name, String description, double mass, double radius, Vector3f axis, Oribit oribit, String parent_name) {
        size++;
        if ("Centre".equals(parent_name)) {
            Planet planet = new Planet(name, description, mass, radius, axis, oribit, Planet.VOID);
            if (name_map.containsKey(name)) name_map.get(name).copy(planet);
            else name_map.put(name, planet);

            if (SUN == null) SUN = name_map.get(name);
            else {
                String errorMsg = String.format("Planets ERROR!! More than one Centre Planet %s and %s was found.", SUN, planet);
                Constants.LOG.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } else {
            if (!name_map.containsKey(name)) name_map.put(name, new Planet());
            if (!name_map.containsKey(parent_name)) name_map.put(parent_name, new Planet());
            Planet parent = name_map.get(parent_name);
            Planet planet = new Planet(name, description, mass, radius, axis, oribit, parent);
            name_map.get(name).copy(planet);
        }
    }


    public static void Check_Display() {
        if (SUN == null) {
            String errorMsg = "Planets ERROR!! No Centre Planets was found.";
            Constants.LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        Planet flag = null;
        for (Planet planet : name_map.values()) {
            Constants.LOG.info("PlanetManager: {} have parent {}", planet.name, planet.parent.name);
            if (planet.mass == 0) {
                flag = planet;
                break;
            }
        }
        if (flag != null) {
            String errorMsg = String.format("Planets ERROR!! found planet %s that have not been fully defined", flag.name);
            Constants.LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }


    public static Planet getPlanet(String name) {
        return name_map.get(name);
    }

    public static Collection<Planet> getPlanets() {
        return name_map.values();
    }



    /**
     * 递归更新所有行星位置
     * @param t 世界时间
     */
    public static void updatePositions(long t) {
        for (Planet planet : name_map.values()) planet.pos_updated = false;
        for (Planet planet : name_map.values())
            if (!planet.pos_updated) planet.updatePosition(t);
        if (size > 0) star_prepared = true;
    }


    /**
     * 获取目标行星在观察者视野中的亮面
     * @param observer 观察者
     * @param target 目标行星
     * @return 亮面类型ID
     */
    public static int getLightPhase(Planet observer, Planet target){
        Planet sun = SUN;
        Vector3f sun_vec = sun.position.sub(target.position, new Vector3f()).normalize();
        Vector3f observer_vec = observer.position.sub(target.position, new Vector3f()).normalize();
        float dot = sun_vec.dot(observer_vec);
        double theta = Math.acos(dot) / util.PI;
        return (int) (theta * 5);
    }

    /**
     * 获取目标行星在观察者视野中的大小
     * @param observer 观察者
     * @param target 目标行星
     * @return 目标视大小
     */
    public static float getApparentSize(Planet observer, Planet target) {
        float distance = observer.position.distance(target.position);
        float k = (float) (target.radius / distance);
        return Math.max(1.024f, k * 2e3f);
    }

    /**
     * 获取目标行星在观察者视野中被太阳光遮蔽的程度
     * @param observer 观察者
     * @param target 目标行星
     * @return 目标被遮蔽导致的不透明度值
     */
    public static float getCoveredBySun(Planet observer, Planet target) {
        Vector3f sun_vec = SUN.position.sub(observer.position, new Vector3f()).normalize();
        Vector3f target_vec = target.position.sub(observer.position, new Vector3f()).normalize();
        return Mth.clamp(1 - sun_vec.dot(target_vec),0.5f, 1f);
    }
}
