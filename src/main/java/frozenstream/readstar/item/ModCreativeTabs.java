package frozenstream.readstar.item;

import frozenstream.readstar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final Supplier<CreativeModeTab> READ_STAR_TAB = CREATIVE_MODE_TABS.register("read_star_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.readstar"))
                    .icon(() -> new ItemStack(ModItems.ASTRONOMICAL_MANUSCRIPT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ASTRONOMICAL_MANUSCRIPT.get());
                        output.accept(ModItems.SUPER_SPYGLASS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
