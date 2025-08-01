package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.StarManager;
import frozenstream.readstar.data.StarPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public record DataPacketAskForStars(List<StarPacket> data) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketAskForStars> CODEC = StreamCodec.composite(
            // 自定义编解码器用于序列化和反序列化 StarData 列表
            StreamCodec.of(
                    (buf, starDataList) -> {
                        buf.writeInt(starDataList.size());
                        for (StarPacket star : starDataList) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, star.name());
                            ByteBufCodecs.STRING_UTF8.encode(buf, star.description());
                            buf.writeDouble(star.position().x);
                            buf.writeDouble(star.position().y);
                            buf.writeDouble(star.position().z);
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<StarPacket> dataList = new java.util.ArrayList<>();
                        for (int cnt = 0; cnt < size; cnt++) {
                            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                            String description = ByteBufCodecs.STRING_UTF8.decode(buf);
                            double position_x = buf.readDouble();
                            double position_y = buf.readDouble();
                            double position_z = buf.readDouble();

                            Vec3 position = new Vec3(position_x, position_y, position_z);

                            dataList.add(new StarPacket(name, description, position));
                        }
                        return dataList;
                    }
            ),
            DataPacketAskForStars::data,
            DataPacketAskForStars::new
    );

    public static final Type<DataPacketAskForStars> TYPE = new Type<>(Constants.PACKET_ID_STAR_ASK);

    @Override
    public void handle(Player player) {
        // 客户端收到数据包后更新本地数据
        Constants.LOG.info("客户端接收到 {} Stars 数据包，更新本地数据...", this.data.size());
        StarManager.init(this.data);
    }

    @Override
    public Type<DataPacketAskForStars> type() {
        return TYPE;
    }
}
