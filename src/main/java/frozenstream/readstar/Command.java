package frozenstream.readstar;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import frozenstream.readstar.data.StarData;
import frozenstream.readstar.data.StarLoader;
import frozenstream.readstar.network.DataPacketAskForStars;
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
                    StarLoader.loadStarData(context.getSource().getServer().getServerDirectory().toAbsolutePath());
                    ServerPlayer player = context.getSource().getPlayer();
                    DataPacketAskForStars packet = new DataPacketAskForStars(StarLoader.getStar_data());
                    Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_STAR_ASK, packet, player);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("show")
                .executes(context -> {
                    for (StarData starData: StarLoader.getStar_data()) {
                        context.getSource().sendSuccess(() -> Component.literal(starData.toString()), false);
                    }
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));

        dispatcher.register(cmd);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        Command.register(event.getDispatcher());
    }
}
