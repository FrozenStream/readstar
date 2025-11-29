package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.element.TimeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public record DataPacketSendTime(long time, long timeAcceleration) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketSendTime> CODEC = StreamCodec.composite(
            StreamCodec.of(FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong),
            DataPacketSendTime::time,
            StreamCodec.of(FriendlyByteBuf::writeLong, FriendlyByteBuf::readLong),
            DataPacketSendTime::timeAcceleration,
            DataPacketSendTime::new
    );

    public static final Type<DataPacketSendTime> TYPE = new Type<>(Constants.PACKET_ID_TIME_SEND);

    @Override
    public void handle(Player player) {
        TimeClient.update(this.time, this.timeAcceleration);
        // TODO: Handle timeAcceleration as needed
    }

    @Override
    public @NotNull Type<DataPacketSendTime> type() {
        return TYPE;
    }
}
