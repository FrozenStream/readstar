package frozenstream.readstar;

import frozenstream.readstar.item.ModCreativeTabs;
import frozenstream.readstar.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Constants.MOD_ID)
public class ReadStarNeoForge {

    public ReadStarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::registerPackets);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }


    public void registerPackets(RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar("1.0.0");

        //Common.registerServerPackets(registrar);
        Common.registerClientPackets(registrar);
    }

}
