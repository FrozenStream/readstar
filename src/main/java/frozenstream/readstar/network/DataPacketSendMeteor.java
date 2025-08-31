package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.meteor.MeteorManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record DataPacketSendMeteor(
        Vector3f startPosition,
        Vector3f direction,
        Vector3f color,
        float pathLength,
        float speed
) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketSendMeteor> CODEC = StreamCodec.composite(
            StreamCodec.of(
                    (buf, vector) -> {
                        buf.writeFloat(vector.x);
                        buf.writeFloat(vector.y);
                        buf.writeFloat(vector.z);
                    },
                    buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
            ),
            DataPacketSendMeteor::startPosition,
            StreamCodec.of(
                    (buf, vector) -> {
                        buf.writeFloat(vector.x);
                        buf.writeFloat(vector.y);
                        buf.writeFloat(vector.z);
                    },
                    buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
            ),
            DataPacketSendMeteor::direction,
            StreamCodec.of(
                    (buf, vector) -> {
                        buf.writeFloat(vector.x);
                        buf.writeFloat(vector.y);
                        buf.writeFloat(vector.z);
                    },
                    buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
            ),
            DataPacketSendMeteor::color,
            StreamCodec.of(RegistryFriendlyByteBuf::writeFloat, RegistryFriendlyByteBuf::readFloat),
            DataPacketSendMeteor::pathLength,
            StreamCodec.of(RegistryFriendlyByteBuf::writeFloat, RegistryFriendlyByteBuf::readFloat),
            DataPacketSendMeteor::speed,
            DataPacketSendMeteor::new
    );

    public static final Type<DataPacketSendMeteor> TYPE = new Type<>(Constants.PACKET_ID_METEOR_SEND);

    @Override
    public void handle(Player player) {
        MeteorManager.addMeteor(startPosition, direction, color, pathLength, speed);
    }

    @Override
    public @NotNull Type<DataPacketSendMeteor> type() {
        return TYPE;
    }
}
