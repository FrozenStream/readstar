package frozenstream.readstar.data.planet;

import frozenstream.readstar.Constants;
import frozenstream.readstar.util;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.*;

//TODO: 添加星球自转周期

public class PlanetManager {
    private static final Map<String, Planet> planets = new TreeMap<>();
    private static final ArrayList<Planet> planets_list = new ArrayList<>();

    public static Planet Root = null;

    public static void init(ArrayList<Planet> in) {
        planets_list.addAll(in);        //复制行星列表
        for (Planet planet : in) {
            planets.put(planet.name, planet);   // 添加 name -> planet 映射
            if(planet.parent == null) Root = planet;    // 设置根节点
        }
        updatePositions(0);
        Constants.LOG.info("PlanetManager: INIT! have {} planets:", planets_list.size());
        planets_list.forEach(planet -> Constants.LOG.info("PlanetManager: {}", planet.name));
    }


    public static Planet getPlanet(String name) {
        return planets.get(name);
    }

    public static ArrayList<Planet> getPlanets() {
        return planets_list;
    }



    /**
     * 递归更新所有行星位置
     * @param t 世界时间
     */
    public static void updatePositions(long t) {
        if(Root.position == null)Root.position = new Vector3f(0,0,0);
        Root.children.forEach(child -> updatePositions(child, t));
    }

    private static void updatePositions(Planet planet, long t) {
        if(planet.position == null)planet.position = new Vector3f();
        if(planet.mass == 0) planet.position.set(0, 0, 0);

        planet.position.set(planet.parent.position).add(planet.oribit.calPosition(planet.parent.mass, t));
        updateNoonVec(planet);
        planet.children.forEach(child -> updatePositions(child, t));
    }

    public static int getPlanetsLevel(String name) {
        return getPlanetsLevel(planets.get(name));
    }

    public static int getPlanetsLevel(Planet planet) {
        if (planet == Root) return 0;
        if (planet.level == -1)
            return planet.level = getPlanetsLevel(planet.parent) + 1;
        else return planet.level;
    }

    public static boolean isSunOrRoot(Planet planet) {
        return getPlanetsLevel(planet) <= 1;
    }

    public static Planet whichIsYourSun(Planet planet) {
        if (planet == Root) throw new RuntimeException("PlanetManager: Root have no sun.");
        if(getPlanetsLevel(planet) == 1) return planet;
        if(planet.mySun ==  null)
            return planet.mySun = whichIsYourSun(planet.parent);
        return planet.mySun;
    }


    /**
     * 更新行星正午朝向向量
     * @param planet 行星
     */
    public static void updateNoonVec(Planet planet) {
        if (planet == Root) return;
        if (planet.Vec_noon == null) planet.Vec_noon = new Vector3f();   //若为空，则创建向量
        Planet parent = whichIsYourSun(planet);
        Vector3f parent_vec = (new Vector3f()).set(parent.position).sub(planet.position);    //获得父级向量
        Vector3f tmp = (new Vector3f()).set(planet.getAxis());   //构建 @parent_vec 平行于 @planet.axis 的向量分量
        float n = planet.getAxis().dot(parent_vec);
        tmp.mul(n);
        planet.Vec_noon.set(parent_vec).sub(tmp);   //减去平行于 @planet.axis 的分量，获得垂直于 @planet.axis 向量分量
        if (planet.Vec_noon.lengthSquared() < 0.01f) planet.Vec_noon.set(0, 1, 0);
        planet.Vec_noon.normalize();
    }


    /**
     * 更新星星当前朝向向量
     * @param planet 行星
     * @return 当前朝向向量
     * */
    public static Vector3f updateCurrentSkyVec(Planet planet, long tick) {
        if (planet.Vec_current == null) planet.Vec_current = new Vector3f();
        float theta = (tick - 6000) * util.PI / 12000;
        Vector3f axis = planet.getAxis();
        planet.Vec_current.set(planet.Vec_noon).rotateAxis(-theta, axis.x, axis.y, axis.z);

        return new Vector3f(planet.Vec_current);
    }


    /**
     * 获取目标行星在观察者视野中的亮面
     * @param observer 观察者
     * @param target 目标行星
     * @return 亮面类型ID
     */
    public static int getLightPhase(Planet observer, Planet target){
        Planet sun = whichIsYourSun(observer);
        Vector3f tar_sun = (new Vector3f()).set(sun.position).sub(target.position).normalize();
        Vector3f tar_obs = (new Vector3f()).set(observer.position).sub(target.position).normalize();
        float dot = tar_sun.dot(tar_obs);
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
     * 获取目标行星在观察者视野中被太阳光掩盖的程度
     * @param observer 观察者
     * @param target 目标行星
     * @return 目标被遮蔽导致的不透明度值
     */
    public static float getCoveredBySun(Planet observer, Planet target) {
        Planet sun = whichIsYourSun(target);
        Vector3f obs_sun = (new Vector3f()).set(sun.position).sub(observer.position).normalize();
        Vector3f obs_tar = (new Vector3f()).set(target.position).sub(observer.position).normalize();
        return Mth.clamp(1 - obs_sun.dot(obs_tar),0.5f, 1f);
    }
}
