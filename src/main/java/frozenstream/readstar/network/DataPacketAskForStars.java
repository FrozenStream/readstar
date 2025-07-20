package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.StarData;
import frozenstream.readstar.data.StarManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;


public record DataPacketAskForStars(List<StarData> data) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketAskForStars> CODEC = StreamCodec.composite(
            // 自定义编解码器用于序列化和反序列化 StarData 列表
            StreamCodec.of(
                (buf, starDataList) -> {
                    buf.writeInt(starDataList.size());
                    for (StarData star : starDataList) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, star.name());
                        ByteBufCodecs.STRING_UTF8.encode(buf, star.description());
                        ByteBufCodecs.STRING_UTF8.encode(buf, star.orbiting());
                        buf.writeDouble(star.mass());
                        buf.writeDouble(star.axis().get(0));
                        buf.writeDouble(star.axis().get(1));
                        buf.writeDouble(star.axis().get(2));
                        buf.writeDouble(star.a());
                        buf.writeDouble(star.e());
                        buf.writeDouble(star.i());
                        buf.writeDouble(star.w());
                        buf.writeDouble(star.o());
                        buf.writeDouble(star.M0());
                    }
                },
                buf -> {
                    int size = buf.readInt();
                    List<StarData> dataList = new java.util.ArrayList<>();
                    for (int cnt = 0; cnt < size; cnt++) {
                        String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                        String description = ByteBufCodecs.STRING_UTF8.decode(buf);
                        String orbiting = ByteBufCodecs.STRING_UTF8.decode(buf);
                        double mass = buf.readDouble();
                        double axis_x = buf.readDouble();
                        double axis_y = buf.readDouble();
                        double axis_z = buf.readDouble();
                        double a = buf.readDouble();
                        double e = buf.readDouble();
                        double i = buf.readDouble();
                        double w = buf.readDouble();
                        double o = buf.readDouble();
                        double M0 = buf.readDouble();

                        ArrayList<Double> axis = new ArrayList<>(){};
                        axis.add(axis_x);
                        axis.add(axis_y);
                        axis.add(axis_z);

                        dataList.add(new StarData(name, description, orbiting, mass, axis, a, e, i, w, o, M0));
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
        Constants.LOG.info("客户端接收到数据包，更新本地数据...");
        StarManager.updateClientData(this.data);
        StarManager.showData();
    }

    @Override
    public Type<DataPacketAskForStars> type() {
        return TYPE;
    }
}
