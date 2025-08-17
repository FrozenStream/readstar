package frozenstream.readstar.data.planet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Constants;
import org.joml.Vector3f;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * JSON行星配置加载器
 * 行星不允许通过配置文件添加或移除
 * @author FrozenStream
 */

//TODO: 添加第三方行星配置API

public class PlanetLoader {

    private static final String planetsPath = "assets/" + Constants.MOD_ID + "/custom/planets/";

    private static final String earth = planetsPath + "Earth.json";
    private static final String sun = planetsPath + "Sun.json";
    private static final String mars = planetsPath + "Mars.json";
    private static final String moon = planetsPath + "Moon.json";


    public static void loadPlanets() {
        readPlanetJson(earth);
        readPlanetJson(sun);
        readPlanetJson(mars);
        readPlanetJson(moon);
    }

    private static void readPlanetJson(String resourcesPath) {
        // 读取文本文件
        InputStream inputStream = PlanetLoader.class.getClassLoader().getResourceAsStream(resourcesPath);
        if (inputStream == null) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // 解析JSON
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // 提取字段
            String name = jsonObject.get("name").getAsString();
            String parent = jsonObject.get("parent").getAsString();
            double mass = jsonObject.get("mass").getAsDouble();
            double radius = jsonObject.get("radius").getAsDouble();

            // 提取axis数组
            JsonArray axisArray = jsonObject.getAsJsonArray("axis");
            Vector3f axis = new Vector3f(
                    axisArray.get(0).getAsFloat(),
                    axisArray.get(1).getAsFloat(),
                    axisArray.get(2).getAsFloat()
            );

            // 提取轨道参数
            double a = jsonObject.get("a").getAsDouble();
            double e = jsonObject.get("e").getAsDouble();
            double i = jsonObject.get("i").getAsDouble();
            double w = jsonObject.get("w").getAsDouble();
            double o = jsonObject.get("o").getAsDouble();
            double M0 = jsonObject.get("M0").getAsDouble();

            Oribit oribit = new Oribit(a, e, i, w, o, M0);

            // 创建Planet
            String descriptionKey = "planetdesc." + Constants.MOD_ID + "." + name.toLowerCase();
            PlanetManager.register(name, descriptionKey, mass, radius, axis, oribit, parent);
            Constants.LOG.info("PlanetLoader: Json load success: {}", resourcesPath);
        } catch (Exception e) {
            Constants.LOG.error("PlanetLoader: Json load fail: {}", resourcesPath, e);
            throw new RuntimeException(e);
        }
    }
}
