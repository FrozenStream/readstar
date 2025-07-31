package frozenstream.readstar;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import frozenstream.readstar.data.PlanetPacket;
import frozenstream.readstar.data.Loader;
import frozenstream.readstar.data.TimeManager;
import frozenstream.readstar.network.DataPacketAskForPlanets;
import frozenstream.readstar.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// Remember that any command is processed in server side


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Command {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("readstar")
            .then(Commands.literal("reload")
                .executes(context -> {
                    Loader.loadData(context.getSource().getServer().getServerDirectory().toAbsolutePath());
                    ServerPlayer player = context.getSource().getPlayer();
                    DataPacketAskForPlanets packet = new DataPacketAskForPlanets(Loader.getPlanet_list());
                    Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_PLANET_ASK, packet, player);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("show")
                .executes(context -> {
                    for (PlanetPacket starData: Loader.getPlanet_list()) {
                        context.getSource().sendSuccess(() -> Component.literal(starData.toString()), false);
                    }
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("reset")
                .executes(context -> {
                    TimeManager.reset();
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));

        dispatcher.register(cmd);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        Command.register(event.getDispatcher());
    }
}
