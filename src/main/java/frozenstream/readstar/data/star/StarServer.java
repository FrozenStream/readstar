package frozenstream.readstar.data.star;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketAskForStars;
import frozenstream.readstar.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.DEDICATED_SERVER)
public class StarServer {
    private static final String starLocation = "/config/"+ Constants.MOD_ID + "/stars/";
    private static final String emptyJson = "tmp.json";

    public static ArrayList<Star> ServerStars = new ArrayList<>();


    public static void loadStarsFromConfig(Path serverPath) {
        File starsDir = new File(serverPath.toFile(), starLocation);
        File[] starFiles;
        try {
            // 检查stars目录存在
            if (!starsDir.exists() || !starsDir.isDirectory()) {
                if (starsDir.mkdirs()) Constants.LOG.info("StarServer: Created stars directory at {}", starsDir);
            }
            // 获取目录中的所有.json文件，如果目录为空，则创建一个临时文件
            starFiles = starsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (starFiles == null) {
                String errorMessage = "StarServer: star folder not found";
                Constants.LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            if (starFiles.length == 0) {
                File tmpFile = new File(starsDir, emptyJson);
                tmpFile.createNewFile();
            } else {
                // 遍历所有.json文件
                for (File starFile : starFiles) {
                    Constants.LOG.info("StarServer: Loading star JSON file: {}", starFile.getName());
                    readStarsJson(starFile);
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("StarServer: Failed to create stars directory at {}", serverPath);
            throw new RuntimeException(e);
        }
    }

    private static void readStarsJson(File starFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(starFile), StandardCharsets.UTF_8))) {
            // 解析JSON
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray starsArray = jsonObject.getAsJsonArray("Stars");
            if (starsArray != null) {
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

                    ServerStars.add(new Star(name, description, position, type));
                }
                Constants.LOG.info("StarServer: Json load success: {}", starFile.getName());
            }
        } catch (Exception e) {
            Constants.LOG.error("StarServer: Json load fail: {}", starFile.getName(), e);
        }
    }


    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path serverPath = event.getServer().getServerDirectory().toAbsolutePath();
        loadStarsFromConfig(serverPath);
    }

    @SubscribeEvent
    public static void onPlayerJoin(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        DataPacketAskForStars starPacket = new DataPacketAskForStars(ServerStars);
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_ASK, starPacket, player);
    }

}
