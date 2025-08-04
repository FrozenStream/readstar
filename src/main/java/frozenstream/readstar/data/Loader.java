package frozenstream.readstar.data;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import frozenstream.readstar.Constants;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Loader {
    // 用于存储读取到的数据
    private static final List<PlanetPacket> planet_list = new ArrayList<>();
    private static final List<StarPacket> star_list = new ArrayList<>();

    private static final Set<String> names_set = new TreeSet<>();

    public Loader() {
        Constants.LOG.info("StarLoader 进行加载");
        names_set.clear();
    }

    public static List<PlanetPacket> getPlanet_list() {
        return planet_list;
    }

    public static List<StarPacket> getStar_list() {
        return star_list;
    }

    public static void registerPlanet(PlanetPacket data) {
        if (names_set.contains(data.name())) {
            Constants.LOG.warn("已忽略重复的Planet: {}", data.name());
            return;
        }
        else {
            Constants.LOG.info("已注册Planet: {}", data.name());
        }
        planet_list.add(data);
        names_set.add(data.name());
    }

    public static void registerStar(StarPacket data) {
        if (names_set.contains(data.name())) {
            Constants.LOG.warn("已忽略重复的Star: {}", data.name());
            return;
        }
        else {
            Constants.LOG.info("已注册Star: {}", data.name());
        }
        star_list.add(data);
        names_set.add(data.name());
    }

    public static void loadData(Path gameDir) {
        Path configDir = gameDir.resolve("config");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            Constants.LOG.error("创建目录失败！");
            throw new RuntimeException(e);
        }

        Path starPath = configDir.resolve("ReadStar-Star.toml");
        Path planetPath = configDir.resolve("ReadStar-Planet.toml");

        String defaultPlanetPath = "assets/" + Constants.MOD_ID + "/config/ReadStar-Planet.toml";
        String defaultStarPath = "assets/" + Constants.MOD_ID + "/config/ReadStar-Star.toml";

        planet_list.clear();
        star_list.clear();

        checkExist(planetPath, defaultPlanetPath);
        checkExist(starPath, defaultStarPath);

        readPlanetConfig(planetPath);
        readStarConfig(starPath);
    }

    private static void checkExist(Path config, String defaultConfig) {
        if (!Files.exists(config)) {
            Constants.LOG.warn("自定义文件不存在！");
            try {
                InputStream is = Loader.class.getClassLoader().getResourceAsStream(defaultConfig);
                if (is == null) {
                    Constants.LOG.error("无法找到默认配置文件！");
                    throw new RuntimeException("无法找到默认配置文件！");
                } else {
                    Files.copy(is, config);
                    Constants.LOG.info("复制文件{} --> {}", defaultConfig, config);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void readPlanetConfig(Path configPath) {
        Constants.LOG.info("开始加载行星配置文件，路径为: {}", configPath);

        TomlParser parser = new TomlParser();

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Config config = parser.parse(reader);
            var starDataArray = config.get("PlanetData");
            if (!(starDataArray instanceof List<?>)) {
                Constants.LOG.error("PlanetData格式错误：没有[[PlanetData]]");
                throw new RuntimeException("PlanetData Config Format Error: No [[PlanetData]] found.");
            }
            for (Object starData : (List<?>) starDataArray) {
                if (starData instanceof Config starConfig) {
                    // 提取每个StarData条目的字段
                    String name = starConfig.get("name");
                    String description = starConfig.get("description");
                    String orbiting = starConfig.get("orbiting");
                    double mass = starConfig.get("mass");
                    ArrayList<Double> tmp = starConfig.get("axis");
                    Vec3 axis = new Vec3(tmp.get(0), tmp.get(1), tmp.get(2));
                    double a = starConfig.get("a");
                    double e = starConfig.get("e");
                    double i = starConfig.get("i");
                    double w = starConfig.get("w");
                    double o = starConfig.get("o");
                    double M0 = starConfig.get("M0");

                    PlanetPacket data = new PlanetPacket(name, description, orbiting, mass, axis, a, e, i, w, o, M0);
                    registerPlanet(data);
                }
            }
        } catch (IOException e) {
            Constants.LOG.error("读取Planet文件失败！");
            throw new RuntimeException(e);
        }
    }


    private static void readStarConfig(Path configPath) {
        Constants.LOG.info("开始加载天球配置文件，路径为: {}", configPath);

        TomlParser parser = new TomlParser();

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Config config = parser.parse(reader);
            var starDataArray = config.get("StarData");
            if (!(starDataArray instanceof List<?>)) {
                Constants.LOG.error("StarData格式错误：没有[[StarData]]");
                throw new RuntimeException("StarData Config Format Error: No [[StarData]] found.");
            }
            for (Object starData : (List<?>) starDataArray) {
                if (starData instanceof Config starConfig) {
                    // 提取每个StarData条目的字段
                    String name = starConfig.get("name");
                    String description = starConfig.get("description");
                    ArrayList<Double> tmp = starConfig.get("position");
                    Vec3 position = new Vec3(tmp.get(0), tmp.get(1), tmp.get(2));
                    int type = starConfig.get("type");

                    StarPacket data = new StarPacket(name, description, position, type);
                    registerStar(data);
                }
            }
        } catch (IOException e) {
            Constants.LOG.error("读取Star文件失败！");
            throw new RuntimeException(e);
        }
    }
}
