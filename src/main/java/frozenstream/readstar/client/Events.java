package frozenstream.readstar.client;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.PlanetLoader;
import frozenstream.readstar.data.PlanetManager;
import frozenstream.readstar.data.StarLoader;
import frozenstream.readstar.data.StarManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Events {

    @SubscribeEvent
    public static void onServerStarting(ClientPlayerNetworkEvent.LoggingIn event) {
        PlanetLoader.loadPlanets();
        PlanetManager.Check_Display();

        StarLoader.loadStars();
        StarManager.Init_Display();
    }
}
