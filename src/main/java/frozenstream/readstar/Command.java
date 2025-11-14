package frozenstream.readstar;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import frozenstream.readstar.data.TimeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// Remember that any command is processed in server side


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Command {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("readstar")
                .then(Commands.literal("set")
                        .then(Commands.argument("time", LongArgumentType.longArg(0))
                                .executes(context -> {
                                    long time = LongArgumentType.getLong(context, "time");
                                    Constants.LOG.info("Command: set time to {}", time);
                                    TimeManager.setTime(time);
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })));
        dispatcher.register(cmd);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        Command.register(event.getDispatcher());
    }
}
