package frozenstream.readstar.data;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import frozenstream.readstar.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StarLoader {
    // 用于存储读取到的数据
    private static final List<StarData> star_data = new ArrayList<>();
    private static final Set<String> star_names_set = new TreeSet<>();

    public StarLoader() {
        Constants.LOG.info("StarLoader 进行加载");
        star_names_set.clear();
    }

    public static List<StarData> getStar_data() {
        return star_data;
    }

    public static void registerStar(StarData data) {
        if (star_names_set.contains(data.name())) {
            Constants.LOG.warn("已忽略重复的StarData: {}", data.name());
            return;
        }
        star_data.add(data);
        star_names_set.add(data.name());
    }

    public static void loadStarData(Path gameDir) {
        Path configDir = gameDir.resolve("config");
        Path configPath = configDir.resolve("ReadStar-StarData.toml");
        String defaultPath = "assets/" + Constants.MOD_ID + "/config/ReadStar-StarData.toml";

        star_data.clear(); // 清空旧数据

        if (!Files.exists(configPath)) {
            Constants.LOG.warn("自定义文件不存在！");
            try {
                Files.createDirectories(configDir);
                InputStream is = StarLoader.class.getClassLoader().getResourceAsStream(defaultPath);
                if (is == null) {
                    Constants.LOG.error("无法找到默认配置文件！");
                    throw new RuntimeException("无法找到默认配置文件！");
                } else {
                    Files.copy(is, configPath);
                    Constants.LOG.info("复制文件{} --> {}", defaultPath, configPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Constants.LOG.info("开始加载自定义文件，路径为: {}", configPath);

        TomlParser parser = new TomlParser();

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Config config = parser.parse(reader);
            var starDataArray = config.get("StarData");
            if (!(starDataArray instanceof List<?>)) {
                Constants.LOG.info("StarData格式错误：没有[[StarData]]\nStarData Config Format Error: No StarData found.");
                throw new RuntimeException("StarData Config Format Error: No [[StarData]] found.");
            }
            for (Object starData : (List<?>) starDataArray) {
                if (starData instanceof Config starConfig) {
                    // 提取每个StarData条目的字段
                    String name = starConfig.get("name");
                    String description = starConfig.get("description");
                    String orbiting = starConfig.get("orbiting");
                    double mass = starConfig.get("mass");
                    ArrayList<Double> axis = starConfig.get("axis");
                    double a = starConfig.get("a");
                    double e = starConfig.get("e");
                    double i = starConfig.get("i");
                    double w = starConfig.get("w");
                    double o = starConfig.get("o");
                    double M0 = starConfig.get("M0");

                    StarData data = new StarData(name, description, orbiting, mass, axis, a, e, i, w, o, M0);
                    registerStar(data);
                }
            }
            for(StarData star: star_data){
                Constants.LOG.info("Server读取数据：{}", star.toString());
            }
        } catch (IOException e) {
            Constants.LOG.error("读取文件失败！");
            throw new RuntimeException(e);
        }
    }
}
