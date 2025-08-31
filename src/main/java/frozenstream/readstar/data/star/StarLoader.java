package frozenstream.readstar.data.star;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Config;
import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketSendStars;
import frozenstream.readstar.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.joml.Vector3f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;



@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class StarLoader {
    private static final String resourceStarPath = "assets/" + Constants.MOD_ID + "/custom/stars/";
    private static final String defaultStars = resourceStarPath + "defaultStars.json";

    private static final String configStarLocation = "/config/"+ Constants.MOD_ID + "/stars/";
    private static final String emptyJson = "tmp.json";

    public static ArrayList<Star> ServerStars = new ArrayList<>();

    public static void loadStars() {
        if (Config.loadDefaultStars) {
            loadResourceStar(defaultStars);
        } else {
            Constants.LOG.info("Skipping default stars loading as per configuration");
        }
    }
    private static void loadResourceStar(String resourcesPath) {
        InputStream inputStream = StarLoader.class.getClassLoader().getResourceAsStream(resourcesPath);
        if (inputStream == null) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            ServerStars.addAll(readStarsJson(reader));
        } catch (Exception e) {
            Constants.LOG.error("StarLoader: Resource Json load fail: {}", resourcesPath);
            throw new RuntimeException(e);
        }
    }


    public static void loadConfigStars(Path serverPath) {
        File starsDir = new File(serverPath.toFile(), configStarLocation);
        checkConfigExist(starsDir);
        File[] starFiles = starsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        // 遍历所有.json文件
        for (File starFile : starFiles) {
            Constants.LOG.info("StarLoader: Config Json loading: {}", starFile.getName());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(starFile), StandardCharsets.UTF_8))) {
                ServerStars.addAll(readStarsJson(reader));
            } catch (Exception e) {
                Constants.LOG.error("StarLoader: Config Json load fail: {}", starFile);
                throw new RuntimeException(e);
            }
        }
    }


    private static void checkConfigExist(File starsDir) {
        // 检查stars目录存在
        if (!starsDir.exists() || !starsDir.isDirectory()) {
            if (starsDir.mkdirs()) Constants.LOG.info("StarLoader: Config stars directory at {} created", starsDir);
        }
        File[] starFiles = starsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        try {
            // 获取目录中的所有.json文件，如果目录为空，则创建一个临时文件
            if (starFiles == null) {
                String errorMessage = "StarLoader: Config star folder not found";
                Constants.LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            if (starFiles.length == 0) {
                File tmpFile = new File(starsDir, emptyJson);
                tmpFile.createNewFile();
            }
        } catch (Exception e) {
            Constants.LOG.error("StarLoader: Config stars directory at {} failed to create", starsDir);
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<Star> readStarsJson(Reader reader) {
        ArrayList<Star> stars = new ArrayList<>();
        JsonObject jsonObject;
        // 解析JSON
        try {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            Constants.LOG.error("StarLoader: Json load fail: {}", reader);
            return stars;
        }
        JsonArray starsArray = jsonObject.getAsJsonArray("Stars");
        for (JsonElement element : starsArray) {
            JsonObject star = element.getAsJsonObject();
            String name = star.get("name").getAsString();
            JsonArray positionArray = star.getAsJsonArray("position");
            Vector3f position = new Vector3f(
                    positionArray.get(0).getAsFloat(),
                    positionArray.get(1).getAsFloat(),
                    positionArray.get(2).getAsFloat()
            );
            int type = star.get("type").getAsInt();
            float Vmag = star.get("Vmag").getAsFloat();

            stars.add(new Star(name, position.normalize(), type, Vmag));
        }
        return stars;
    }


    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path serverPath = event.getServer().getServerDirectory().toAbsolutePath();

        ServerStars.clear();
        loadConfigStars(serverPath);
        loadStars();
        Constants.LOG.info("StarLoader: Loaded {} stars", ServerStars.size());
    }

    @SubscribeEvent
    public static void onPlayerJoin(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        DataPacketSendStars starPacket = new DataPacketSendStars(ServerStars);
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_SEND, starPacket, player);
    }
}