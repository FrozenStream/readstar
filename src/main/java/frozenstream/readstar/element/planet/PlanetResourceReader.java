package frozenstream.readstar.element.planet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Optional;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class PlanetResourceReader extends SimplePreparableReloadListener<ArrayList<Planet>> {
    private static final String systemRoot = "custom/planets/";
    public static final ArrayList<Planet> PLANETS = new ArrayList<>();
    static Boolean readFailed = false;


    @Override
    protected ArrayList<Planet> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.push("planet prepare");
        ArrayList<Planet> planets = new ArrayList<>();  // 创建一个空的行星列表
        Planet RootPlanet = Planet.createRoot();        // 创建根节点
        planets.add(RootPlanet);

        ResourceLocation system = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, systemRoot + "system.json");
        try {
            Optional<Resource> resourceOpt = resourceManager.getResource(system);
            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                Constants.LOG.info("PlanetLoader: Ready Building planets");
                BufferedReader reader = resource.openAsReader();
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                buildPlanets(root, RootPlanet, planets, resourceManager); // 递归处理JSON
            } else Constants.LOG.info("PlanetLoader: read planet json failed");
        } catch (Exception e) {
            Constants.LOG.error("Error reading custom.json: {}", e.getMessage());
        }
        profilerFiller.pop();
        return planets;
    }


    @Override
    protected void apply(ArrayList<Planet> planets, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        if(readFailed) throw new RuntimeException("PlanetLoader: read planet json failed, not exist!");
        PLANETS.clear();
        PLANETS.addAll(planets);
    }


    private static void buildPlanets(JsonObject object, Planet root, ArrayList<Planet> in, ResourceManager resourceManager) {
        Constants.LOG.info("PlanetLoader: Building Planet {}, root JSON {}", root.name, object.toString());
        for (String row_name : object.keySet()) {
            String name = row_name.toLowerCase();
            Planet single = new Planet(name, root);             // 创建行星对象
            ResourceLocation detailFile = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, systemRoot + name + ".json");
            Optional<Resource> resourceOpt = resourceManager.getResource(detailFile);   // 获取JSON子节点的资源

            if (resourceOpt.isEmpty()) {                        // 判断资源是否存在
                Constants.LOG.error("PlanetLoader: {}.json not found", name);
                readFailed = true;

            }
            else getPlanetsDetails(resourceOpt.get(), single);       // 获取JSON子节点的资源

            root.children.add(single);
            in.add(single);     // 在总列表中添加行星对象
            JsonObject planet = object.getAsJsonObject(row_name);   // 获取子节点
            buildPlanets(planet, single, in, resourceManager);    // 递归处理子节点
        }
    }

    private static void getPlanetsDetails(Resource resource, Planet planet) {
        Constants.LOG.info("PlanetLoader: Reading Json {}.json", planet.name);
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            // 提取字段
            double mass = root.get("mass").getAsDouble();
            double radius = root.get("radius").getAsDouble();

            // 提取axis数组
            JsonArray axisArray = root.getAsJsonArray("axis");
            Vector3f axis = new Vector3f(
                    axisArray.get(0).getAsFloat(),
                    axisArray.get(1).getAsFloat(),
                    axisArray.get(2).getAsFloat()
            ).normalize();

            // 提取轨道参数
            double a = root.get("a").getAsDouble();
            double e = root.get("e").getAsDouble();
            double i = root.get("i").getAsDouble();
            double w = root.get("w").getAsDouble();
            double o = root.get("o").getAsDouble();
            double M0 = root.get("M0").getAsDouble();

            Oribit oribit = new Oribit(a, e, i, w, o, M0);

            planet.mass = mass;
            planet.radius = radius;
            planet.axis = axis;
            planet.oribit = oribit;

        } catch (Exception e) {
            Constants.LOG.error("Error reading {}: {}", planet.name, e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onRegisterDataPackReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new PlanetResourceReader());
    }
}
