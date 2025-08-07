package frozenstream.readstar.events;

import frozenstream.readstar.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FovEvent {
    public static double fov = 70;
    @SubscribeEvent
    public static void handeFOVModifier(ViewportEvent.ComputeFov event) {
        fov = event.getFOV();
    }
}
