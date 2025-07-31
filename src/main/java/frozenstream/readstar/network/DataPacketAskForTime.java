package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.TimeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public record DataPacketAskForTime(long data) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketAskForTime> CODEC = StreamCodec.composite(
            StreamCodec.of(FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong),
            DataPacketAskForTime::data,
            DataPacketAskForTime::new
    );

    public static final Type<DataPacketAskForTime> TYPE = new Type<>(Constants.PACKET_ID_TIME_ASK);

    @Override
    public void handle(Player player) {
        TimeClient.updateTime(this.data);
    }

    @Override
    public @NotNull Type<DataPacketAskForTime> type() {
        return TYPE;
    }
}
