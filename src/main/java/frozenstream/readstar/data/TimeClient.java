package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TimeClient {
    private static long time = 0;
    private static long counter = 0;
    private static final int UPDATE_INTERVAL_TICKS = 1; // 每100 ticks更新一次
    private static final Minecraft mc = Minecraft.getInstance();

    public static void updateTime(long time)
    {
        Constants.LOG.info("更新时间：{}", time);
        TimeClient.time = time - 1;
        PlanetManager.updatePositions(TimeClient.time);
        counter = 10000;
    }

    @SubscribeEvent
    public static void onServerTick(ClientTickEvent.Post event) {
        if (mc.level == null || mc.player == null || mc.isPaused()) return;
        time++;
        counter++;
        if (counter >= UPDATE_INTERVAL_TICKS) {
            PlanetManager.updatePositions(time);
            counter = 0;
        }
    }
}
