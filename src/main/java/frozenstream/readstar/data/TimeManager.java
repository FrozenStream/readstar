package frozenstream.readstar.data;


import frozenstream.readstar.Config;
import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketAskForTime;
import frozenstream.readstar.platform.Services;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TimeManager {
    private static long counter = 100000000;
    private static final int UPDATE_INTERVAL_TICKS = 8000;

    private static long timeOffset = 0;
    private static long time;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if (counter > UPDATE_INTERVAL_TICKS) {
            time = timeOffset + System.currentTimeMillis() / 1000L * Config.timeAcceleration;
            DataPacketAskForTime timePacket = new DataPacketAskForTime(time);
            PlayerList players = event.getServer().getPlayerList();
            for(var player : players.getPlayers()){
                Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_TIME_ASK, timePacket, player);
            }
            counter = 0;
            Constants.LOG.info("TimeManager: update time：{}", time);
        }
    }

    public static void setTimeOffset(long offset) {
        timeOffset = offset;
    }
}
