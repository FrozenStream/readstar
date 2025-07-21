package frozenstream.readstar;


import frozenstream.readstar.data.StarLoader;
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


import java.nio.file.Path;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Events {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Path gameDir = event.getServer().getServerDirectory().toAbsolutePath();
        StarLoader.loadStarData(gameDir);
    }

    @SubscribeEvent
    public static void onPlayerJoin(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        DataPacketAskForStars packet = new DataPacketAskForStars(StarLoader.getStar_data());
        // 发送数据包给加入的玩家
        Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_ASK, packet, player);
    }

    @SubscribeEvent
    public static void onClientTick(LevelEvent.Load event) {

        Level level = (Level) event.getLevel();
        ResourceKey<Level> dimensionType = level.dimension();
        if(dimensionType == Level.OVERWORLD) level.setDayTimePerTick(100);

    }
}
