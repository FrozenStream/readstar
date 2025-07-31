package frozenstream.readstar.data;


import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketAskForTime;
import frozenstream.readstar.platform.Services;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TimeManager {
    private static final String DATA_NAME = "readstar_time_data";
    private static long time = 0;
    private static long counter = -1000;
    private static final int UPDATE_INTERVAL_TICKS = 100; // 每100 ticks更新一次


    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 服务器启动时加载保存的时间数据
        ServerLevel overworld = event.getServer().overworld();
        TimeData timeData = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TimeData::new, (tag, provider) -> TimeData.load(tag)), DATA_NAME);
        time = timeData.getTime() - 1;
        counter = 100000;

        Constants.LOG.info("Loaded time data: {}", time);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        time++;
        counter++;
        if (counter >= UPDATE_INTERVAL_TICKS) {
            DataPacketAskForTime timePacket = new DataPacketAskForTime(time);
            PlayerList players = event.getServer().getPlayerList();
            for(var player : players.getPlayers()){
                Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_TIME_ASK, timePacket, player);
            }
            counter = 0;
            Constants.LOG.info("TimeManager update time：{}", time);

            // 保存时间数据
            ServerLevel overworld = event.getServer().overworld();
            TimeData timeData = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(TimeData::new, (tag, provider) -> TimeData.load(tag)), DATA_NAME);
            timeData.setTime(time);
        }
    }

    public static void reset() {
        time = -1;
        counter = 100000;
    }



    public static class TimeData extends SavedData {
        private long time = 0;

        public TimeData() {
        }

        public TimeData(CompoundTag nbt) {
            this.time = nbt.getLong("Time");
        }

        @Override
        public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
            Constants.LOG.warn("TimeData save {}", time);
            compoundTag.putLong("Time", this.time);
            return compoundTag;
        }

        public static TimeData load(CompoundTag nbt) {
            return new TimeData(nbt);
        }

        public long getTime(){
            return time;
        }

        public void setTime(long time) {
            this.setDirty();
            this.time = time;
        }
    }
}
