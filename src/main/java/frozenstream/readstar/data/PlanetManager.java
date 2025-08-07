package frozenstream.readstar.data;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;


public class PlanetManager {
    public static final float PI = 3.14159265358979f;
    public static boolean star_prepared = false;
    private static int size = 0;
    private static final Map<String, Planet> name_map = new TreeMap<>();

    public static Planet SUN = null;

    public static void init(List<PlanetPacket> starList) {
        size = starList.size();
        for(PlanetPacket star : starList){
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
        getSUN();
    }

    public static Planet getSUN() {
        if (SUN != null) return SUN;
        for (Planet planet : name_map.values())
            if (planet.parent == Planet.VOID) {
                SUN = planet;
                return SUN;
            }
        Constants.LOG.error("未找到太阳！请检查行星树是否完整！");
        return null;
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


    public static void updatePositions(long t) {
        for (Planet planet : name_map.values()) planet.pos_updated = false;
        for (Planet planet : name_map.values())
            if (!planet.pos_updated) planet.updatePosition(t);
        if (size > 0) star_prepared = true;
    }


    public static int getLightPhase(Planet observer, Planet target){
        Planet sun = getSUN();
        Vector3f sun_vec = sun.position.sub(observer.position, new Vector3f()).normalize();
        Vector3f observer_vec = observer.position.sub(target.position, new Vector3f()).normalize();
        float dot = sun_vec.dot(observer_vec);
        double theta = Math.acos(dot) / PI;
        return (int) (theta * 5);
    }


    public static void drawPlanets(Tesselator tesselator, Planet observer, PoseStack.Pose pose, long t, float rain) {
        if (!PlanetManager.star_prepared) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Vector2f[] uvs = new Vector2f[4];

        for (Planet planet : name_map.values()) {
            if (planet == observer) continue;

            Vector3f vec = planet.position.sub(observer.position, new Vector3f()).normalize(100.0f);
            Quaternionf quaternion = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vec);
            RenderSystem.setShaderTexture(0, Textures.getTexture(planet.name));
            BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            if (planet == SUN) {
                uvs[0] = new Vector2f(1, 0);
                uvs[1] = new Vector2f(1, 1);
                uvs[2] = new Vector2f(0, 1);
                uvs[3] = new Vector2f(0, 0);
                float s = 40;
                Vector3f[] v = new Vector3f[4];
                v[0] = (new Vector3f(s, -s, 0.0F)).rotate(quaternion);
                v[1] = (new Vector3f(s, s, 0.0F)).rotate(quaternion);
                v[2] = (new Vector3f(-s, s, 0.0F)).rotate(quaternion);
                v[3] = (new Vector3f(-s, -s, 0.0F)).rotate(quaternion);

                vec.normalize(400.0f);
                builder.addVertex(pose, vec.add(v[0], new Vector3f())).setUv(uvs[3].x, uvs[3].y);
                builder.addVertex(pose, vec.add(v[1], new Vector3f())).setUv(uvs[0].x, uvs[0].y);
                builder.addVertex(pose, vec.add(v[2], new Vector3f())).setUv(uvs[1].x, uvs[1].y);
                builder.addVertex(pose, vec.add(v[3], new Vector3f())).setUv(uvs[2].x, uvs[2].y);

            } else {
                uvs = Textures.getp(getLightPhase(observer, planet));

                float s = 10;
                Vector3f[] v = new Vector3f[4];

                Vector3f planet_sun = SUN.position.sub(planet.position, new Vector3f()).normalize();
                Vector3f vec_n = vec.normalize(new Vector3f());
                Vector3f project = vec_n.mul(planet_sun.dot(vec_n), new Vector3f());
                planet_sun.sub(project).normalize();

                Vector3f ano = planet_sun.cross(vec_n, new Vector3f()).normalize();

                v[0] = planet_sun.sub(ano, new Vector3f()).mul(s);
                v[1] = planet_sun.add(ano, new Vector3f()).mul(s);
                planet_sun.mul(-1);
                v[2] = planet_sun.add(ano, new Vector3f()).mul(s);
                v[3] = planet_sun.sub(ano, new Vector3f()).mul(s);

                Constants.LOG.info("planet_sun {}, ano {}", planet_sun, ano);


                builder.addVertex(pose, vec.add(v[0], new Vector3f())).setUv(uvs[1].x, uvs[1].y);
                builder.addVertex(pose, vec.add(v[1], new Vector3f())).setUv(uvs[2].x, uvs[2].y);
                builder.addVertex(pose, vec.add(v[2], new Vector3f())).setUv(uvs[3].x, uvs[3].y);
                builder.addVertex(pose, vec.add(v[3], new Vector3f())).setUv(uvs[0].x, uvs[0].y);

            }
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }
    }


    private static final float SUNRISE_START = 23500;
    private static final float SUNRISE_END = 500;
    private static final float SUNSET_START = 11500;
    private static final float SUNSET_END = 12500;
    private static final float DAYTIME = 24000;
    private static float getAlpha(long t) {
        if (t >= 0 && t < SUNRISE_END) return 0.5f - t / SUNRISE_END * 0.5f;
        if (t >= SUNRISE_END && t < SUNSET_START) return 0f;
        if (t >= SUNSET_START && t < SUNSET_END) return (t - SUNSET_START) / (SUNSET_END - SUNSET_START);
        if (t >= SUNSET_END && t < SUNRISE_START) return 1f;
        return 1f - (t - SUNRISE_START) / (DAYTIME - SUNRISE_START) * 0.5f;
    }
}
