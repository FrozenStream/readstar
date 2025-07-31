package frozenstream.readstar;


import frozenstream.readstar.data.Loader;
import frozenstream.readstar.network.DataPacketAskForPlanets;
import frozenstream.readstar.network.DataPacketAskForStars;
import frozenstream.readstar.platform.Services;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;


import java.nio.file.Path;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Events {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path gameDir = event.getServer().getServerDirectory().toAbsolutePath();
        Loader.loadData(gameDir);
    }

    @SubscribeEvent
    public static void onPlayerJoin(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        DataPacketAskForPlanets planetPacket = new DataPacketAskForPlanets(Loader.getPlanet_list());
        DataPacketAskForStars starPacket = new DataPacketAskForStars(Loader.getStar_list());
        // 发送数据包给加入的玩家
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_PLANET_ASK, planetPacket, player);
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_ASK, starPacket, player);
    }

    @SubscribeEvent
    public static void onClientTick(LevelEvent.Load event) {

        Level level = (Level) event.getLevel();
        ResourceKey<Level> dimensionType = level.dimension();
        if(dimensionType == Level.OVERWORLD) level.setDayTimePerTick(10);

    }



}
