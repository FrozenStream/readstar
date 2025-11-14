package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.PlanetManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TimeClient {
    private static long time = 0;
    private static long counter = 0;
    private static long timeAcceleration = 1;
    private static final int UPDATE_INTERVAL_TICKS = 10; // 每100 ticks更新一次
    private static final Minecraft mc = Minecraft.getInstance();

    public static void update(long time, long timeAcceleration) {
        Constants.LOG.info("TimeClient：update time: {}, update timeAcceleration {}", time, timeAcceleration);
        TimeClient.time = time - 1;
        TimeClient.timeAcceleration = timeAcceleration;
        PlanetManager.updatePositions(TimeClient.time);
        counter = 10000;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        time += timeAcceleration;
        counter++;
        if (counter >= UPDATE_INTERVAL_TICKS) {
            PlanetManager.updatePositions(time);
            counter = 0;
        }
    }
}
