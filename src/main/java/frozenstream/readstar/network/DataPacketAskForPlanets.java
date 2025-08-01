package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.PlanetManager;
import frozenstream.readstar.data.PlanetPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public record DataPacketAskForPlanets(List<PlanetPacket> data) implements PacketBase {

    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketAskForPlanets> CODEC = StreamCodec.composite(
            // 自定义编解码器用于序列化和反序列化 StarData 列表
            StreamCodec.of(
                (buf, planetList) -> {
                    buf.writeInt(planetList.size());
                    for (PlanetPacket planet : planetList) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, planet.name());
                        ByteBufCodecs.STRING_UTF8.encode(buf, planet.description());
                        ByteBufCodecs.STRING_UTF8.encode(buf, planet.parent());
                        buf.writeDouble(planet.mass());
                        buf.writeDouble(planet.axis().x);
                        buf.writeDouble(planet.axis().y);
                        buf.writeDouble(planet.axis().z);
                        buf.writeDouble(planet.a());
                        buf.writeDouble(planet.e());
                        buf.writeDouble(planet.i());
                        buf.writeDouble(planet.w());
                        buf.writeDouble(planet.o());
                        buf.writeDouble(planet.M0());
                    }
                },
                buf -> {
                    int size = buf.readInt();
                    List<PlanetPacket> dataList = new java.util.ArrayList<>();
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

                        Vec3 axis = new Vec3(axis_x, axis_y, axis_z);

                        dataList.add(new PlanetPacket(name, description, orbiting, mass, axis, a, e, i, w, o, M0));
                    }
                    return dataList;
                }
            ),
            DataPacketAskForPlanets::data,
            DataPacketAskForPlanets::new
    );

    public static final Type<DataPacketAskForPlanets> TYPE = new Type<>(Constants.PACKET_ID_PLANET_ASK);

    @Override
    public void handle(Player player) {
        // 客户端收到数据包后更新本地数据
        Constants.LOG.info("客户端接收到 {} Planets 数据包，更新本地数据...", this.data.size());
        PlanetManager.init(this.data);
    }

    @Override
    public Type<DataPacketAskForPlanets> type() {
        return TYPE;
    }
}
