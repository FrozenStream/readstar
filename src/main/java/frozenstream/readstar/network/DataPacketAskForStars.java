package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.star.Star;
import frozenstream.readstar.data.star.StarManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.List;


public record DataPacketAskForStars(List<Star> data) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketAskForStars> CODEC = StreamCodec.composite(
            // 自定义编解码器用于序列化和反序列化 StarData 列表
            StreamCodec.of(
                    (buf, starDataList) -> {
                        buf.writeInt(starDataList.size());
                        for (Star star : starDataList) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, star.name());
                            buf.writeFloat(star.position().x);
                            buf.writeFloat(star.position().y);
                            buf.writeFloat(star.position().z);
                            buf.writeInt(star.type());
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<Star> dataList = new java.util.ArrayList<>();
                        for (int cnt = 0; cnt < size; cnt++) {
                            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                            float position_x = buf.readFloat();
                            float position_y = buf.readFloat();
                            float position_z = buf.readFloat();
                            int type = buf.readInt();
                            Vector3f position = new Vector3f(position_x, position_y, position_z);

                            dataList.add(new Star(name, position, type));
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
        Constants.LOG.info("客户端接收到 {} 来自Server 的 Stars 数据包，更新本地数据...", this.data.size());
        StarManager.init();
        for(Star star : this.data) StarManager.register(star);
        if(!this.data.isEmpty()) StarManager.Display_Build();
    }

    @Override
    public Type<DataPacketAskForStars> type() {
        return TYPE;
    }
}
