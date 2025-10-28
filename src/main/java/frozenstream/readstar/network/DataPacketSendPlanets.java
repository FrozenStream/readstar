package frozenstream.readstar.network;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.Oribit;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.data.planet.PlanetManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;

public record DataPacketSendPlanets(ArrayList<Planet> planets) implements PacketBase {
    public static final StreamCodec<RegistryFriendlyByteBuf, DataPacketSendPlanets> CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        // 编码行星列表大小
                        buf.writeInt(packet.planets.size());

                        HashMap<Planet, Integer> map = new HashMap<>();
                        int index = 0;
                        for (Planet planet : packet.planets) {
                            map.put(planet, index++);
                        }



                        // 编码每个行星
                        for (Planet planet : packet.planets) {
                            if(planet == packet.planets.getFirst()) continue;   // 跳过PlanetRoot
                            buf.writeUtf(planet.name);  // 使用 writeUtf 而不是 writeString
                            buf.writeDouble(planet.mass);
                            buf.writeDouble(planet.radius);
                            buf.writeVector3f(planet.axis);
                            Oribit.ORBIT_CODEC.encode(buf, planet.oribit);

                            buf.writeInt(map.get(planet.parent));
                            buf.writeInt(planet.children.size());
                            for (Planet child : planet.children) buf.writeInt(map.get(child));
                        }
                    },
                    buf -> {
                        // 解码行星列表
                        int size = buf.readInt();
                        Constants.LOG.info("NetWorkSend: Planet list buf received: {}", size);
                        ArrayList<Planet> planets = new ArrayList<>();
                        ArrayList<Integer> parent_index = new ArrayList<>();
                        ArrayList<ArrayList<Integer>> children_index = new ArrayList<>();

                        planets.add(Planet.createRoot());           //PlanetRoot永远是第0个
                        parent_index.add(-1);                       //创建PlanetRoot的父级索引为-1
                        children_index.add(new ArrayList<>());      //创建PlanetRoot的子级索引为空
                        for (int i = 1; i < size; i++) {
                            // 解码每个行星
                            String name = buf.readUtf();  // 使用 readUtf 而不是 readString
                            double mass = buf.readDouble(); //读取质量
                            double radius = buf.readDouble();   //读取半径
                            var axis = buf.readVector3f();      //读取轴向
                            Oribit oribit = Oribit.ORBIT_CODEC.decode(buf); //读取轨道参数

                            parent_index.add(buf.readInt());    //读取父级索引
                            int children_size = buf.readInt();  //读取子级数量
                            children_index.add(new ArrayList<>());
                            for (int j = 0; j < children_size; j++) children_index.getLast().add(buf.readInt());

                            Constants.LOG.info("NetWorkSend: Planet {} get parent {}, children {}", name, parent_index.getLast(), children_index.getLast());


                            // 创建行星对象
                            Planet planet = new Planet(name, mass, radius, axis, oribit);
                            planets.add(planet);
                        }

                        for (int i = 1; i < size; i++) {
                            Planet planet = planets.get(i);
                            int parent_id = parent_index.get(i);
                            planet.parent = planets.get(parent_id);
                            if (parent_id == 0) planets.getFirst().children.add(planet);

                            int children_size = children_index.get(i).size();
                            for (int j = 0; j < children_size; j++)
                                planet.children.add(planets.get(children_index.get(i).get(j)));
                        }

                        return new DataPacketSendPlanets(planets);
                    }
            );



    public static final Type<DataPacketSendPlanets> TYPE = new CustomPacketPayload.Type<>(Constants.PACKET_ID_PLANETS_SEND);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(Player player) {
        PlanetManager.init(planets);
    }
}
