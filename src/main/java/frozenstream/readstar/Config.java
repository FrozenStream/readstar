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

    private static final ModConfigSpec.LongValue TIME_ACCELERATION = BUILDER
            .comment("Time acceleration factor.")
            .defineInRange("timeAcceleration", 1L, 1L, 1000L);


    private static final ModConfigSpec.DoubleValue METEOR_PROBABILITY = BUILDER
            .comment("Meteor occurred probability.")
            .defineInRange("MeteorProbability", 0.2, 0.0, 0.7);


    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");


    static final ModConfigSpec SPEC = BUILDER.build();

    public static long timeAcceleration;
    public static double meteorProbability;

    public static String magicNumberIntroduction;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        timeAcceleration = TIME_ACCELERATION.get();
        meteorProbability = METEOR_PROBABILITY.get();

        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
    }
}
