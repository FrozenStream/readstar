package frozenstream.readstar.element;

import frozenstream.readstar.Config;
import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketSendTime;
import frozenstream.readstar.platform.Services;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TimeManager {
    private static final String DATA_NAME = "readstar_time_data";
    private static long counter = 100000000;
    private static final int UPDATE_INTERVAL_TICKS = 100;

    private static long time;

    // 添加持久化数据管理器
    private static TimeStore data_store;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        time += Config.timeAcceleration;
        setTime(time);
        if (counter > UPDATE_INTERVAL_TICKS) {
            DataPacketSendTime timePacket = new DataPacketSendTime(time, Config.timeAcceleration);
            PlayerList players = event.getServer().getPlayerList();
            for (var player : players.getPlayers()) {
                Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_TIME_SEND, timePacket, player);
            }
            counter = 0;
            Constants.LOG.info("TimeManager: update time：{}", time);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null) {
            DimensionDataStorage storage = overworld.getDataStorage();
            data_store = storage.computeIfAbsent(TimeStore.factory(), DATA_NAME);
            time = data_store.getTime();
            Constants.LOG.info("TimeManager: loaded time: {}", time);
        }
    }

    public static void setTime(long time) {
        TimeManager.time = time;
        data_store.setTime(time);
    }

    /**
     * 时间数据管理器，用于持久化存储时间偏移量
     */
    public static class TimeStore extends SavedData {
        private long time = 0;

        public TimeStore() {
            // 默认构造函数
        }

        public TimeStore(CompoundTag compoundTag, HolderLookup.Provider provider) {
            time = compoundTag.getLong(DATA_NAME);
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
            tag.putLong(DATA_NAME, time);
            return tag;
        }

        public static SavedData.Factory<TimeStore> factory() {
            return new SavedData.Factory<>(TimeStore::new, TimeStore::new, null);
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
            setDirty();
        }
    }
}