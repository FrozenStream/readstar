package frozenstream.readstar.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Constants;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StarLoader {

    private static final String starsPath = "assets/" + Constants.MOD_ID + "/custom/stars/";
    private static final String defaultStars = starsPath + "defaultStars.json";

    public static void loadStars() {
        readStarsJson(defaultStars);
    }
    private static void readStarsJson(String resourcesPath) {
        // 读取文本文件
        InputStream inputStream = PlanetLoader.class.getClassLoader().getResourceAsStream(resourcesPath);
        if (inputStream == null) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // 解析JSON
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray starsArray = jsonObject.getAsJsonArray("Stars");
            for(JsonElement element : starsArray){
                JsonObject star = element.getAsJsonObject();
                String name = star.get("name").getAsString();
                String description = star.get("description").getAsString();

                JsonArray positionArray = star.getAsJsonArray("position");
                Vector3f position = new Vector3f(
                        positionArray.get(0).getAsFloat(),
                        positionArray.get(1).getAsFloat(),
                        positionArray.get(2).getAsFloat()
                );

                int type = star.get("type").getAsInt();

                StarManager.register(name, description, position, type);
            }
            Constants.LOG.info("StarLoader: Json load success: {}", resourcesPath);
        } catch (Exception e) {
            Constants.LOG.error("StarLoader: Json load fail: {}", resourcesPath, e);
            throw new RuntimeException(e);
        }
    }
}
