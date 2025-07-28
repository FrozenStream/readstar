package frozenstream.readstar.data;


import frozenstream.readstar.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientDataEvent {
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL_TICKS = 10; // 每100 ticks更新一次

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (++tickCounter >= UPDATE_INTERVAL_TICKS) {
            tickCounter = 0;
            double currentTime = System.currentTimeMillis() / 1000.0; // 时间单位转换为秒
            PlanetManager.updatePositions(currentTime);
        }
    }

}
