package frozenstream.readstar;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOAD_DEFAULT_STARS = BUILDER
            .comment("Whether to load default stars from the mod's resources")
            .define("loadDefaultStars", true);

    private static final ModConfigSpec.LongValue TIME_ACCELERATION = BUILDER
            .comment("Time acceleration factor. 1.0 = real time, 2.0 = twice as fast, 0.5 = half speed")
            .defineInRange("timeAcceleration", 1L, 1L, 1000L);


    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean loadDefaultStars;
    public static long timeAcceleration;

    public static String magicNumberIntroduction;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        loadDefaultStars = LOAD_DEFAULT_STARS.get();
        timeAcceleration = TIME_ACCELERATION.get();

        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
    }
}
