package frozenstream.readstar.data.meteor;

import frozenstream.readstar.Config;
import frozenstream.readstar.Constants;
import frozenstream.readstar.network.DataPacketSendMeteor;
import frozenstream.readstar.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MeteorTrigger {
    private static final RandomSource random = RandomSource.create(10842L);

    public static void init(PlayerList playerList) {
        Vector3f startPosition = new Vector3f(
                random.nextFloat() * 2.0f - 1.0f,
                random.nextFloat() * 2.0f - 1.0f,
                random.nextFloat() * 2.0f - 1.0f
        ).normalize(95.0f);
        Vector3f direction = new Vector3f(
                random.nextFloat() * 2.0f - 1.0f,
                random.nextFloat() * 2.0f - 1.0f,
                random.nextFloat() * 2.0f - 1.0f
        ).normalize();
        Vector3f color = new Vector3f(
                0.8f + random.nextFloat() * 0.2f,
                0.5f + random.nextFloat() * 0.5f,
                0.2f + random.nextFloat() * 0.3f
        );
        float pathLength = 20.0f + random.nextFloat() * 10.0f;
        float speed = pathLength * (3 + random.nextFloat());

        DataPacketSendMeteor packet = new DataPacketSendMeteor(startPosition, direction, color, pathLength, speed);
        for (ServerPlayer player : playerList.getPlayers()) {
            Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_SEND, packet, player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if(random.nextFloat() < Config.meteorProbability){
            init(event.getServer().getPlayerList());
        }
    }
}
