package frozenstream.readstar.data;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;


public class PlanetManager {
    public static final float PI = 3.14159265358979f;
    public static boolean star_prepared = false;
    private static int size = 0;
    private static final Map<String, Planet> name_map = new TreeMap<>();

    public static final Map<String, VertexBuffer> buffer_map = new TreeMap<>();

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
        for(Planet planet : name_map.values()) {
            buffer_map.put(planet.name, new VertexBuffer(VertexBuffer.Usage.STATIC));
        }
        checkAndDisplay();
    }

    public static Planet getPlanet(String name) {
        return name_map.get(name);
    }

    public static Collection<Planet> getAllPlanets() {
        return  name_map.values();
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


    public static void drawStars(Tesselator tesselator, Planet observer, PoseStack.Pose pose) {
        if (PlanetManager.star_prepared) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            for (Planet planet : name_map.values()) {
                if (planet == observer) continue;
                Vector3f vec = planet.position.sub(observer.position, new Vector3f()).normalize(100.0f);
                Quaternionf quat = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -100.0F), vec);
                RenderSystem.setShaderTexture(0, Textures.getTexture(planet.name));
                BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                float s = 10;
                builder.addVertex(pose, vec.add( (new Vector3f(s, -s, 0.0F)).rotate(quat) )).setUv(1.0F, 0.0F);
                builder.addVertex(pose, vec.add( (new Vector3f(s, s, 0.0F)).rotate(quat) )).setUv(1.0F, 1.0F);
                builder.addVertex(pose, vec.add( (new Vector3f(-s, s, 0.0F)).rotate(quat) )).setUv(0.0F, 1.0F);
                builder.addVertex(pose, vec.add( (new Vector3f(-s, -s, 0.0F)).rotate(quat) )).setUv(0.0F, 0.0F);
                BufferUploader.drawWithShader(builder.buildOrThrow());
            }
        }
    }
}
