package frozenstream.readstar;

import frozenstream.readstar.element.planet.Planet;
import frozenstream.readstar.element.planet.PlanetResourceReader;
import frozenstream.readstar.network.DataPacketSendPlanets;
import frozenstream.readstar.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Events {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //发送Planets
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ArrayList<Planet> data = PlanetResourceReader.PLANETS;
        Constants.LOG.info("Sending {} Planets to {}",data.size(), player.getName().getString());
        DataPacketSendPlanets packet = new DataPacketSendPlanets(data);
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_PLANETS_SEND, packet, player);
    }
}
