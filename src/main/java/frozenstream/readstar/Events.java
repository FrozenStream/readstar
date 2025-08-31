package frozenstream.readstar;

import frozenstream.readstar.data.planet.PlanetLoader;
import frozenstream.readstar.data.planet.PlanetManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Events {

    @SubscribeEvent
    public static void onServerStarting(ClientPlayerNetworkEvent.LoggingIn event) {
        PlanetLoader.loadPlanets();
        PlanetManager.Check_Display();
    }
}
