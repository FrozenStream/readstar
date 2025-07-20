package frozenstream.readstar.item;

import frozenstream.readstar.Constants;
import frozenstream.readstar.item.superSpyglass.SuperSpyglassItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    public static final DeferredItem<Item> SUPER_SPYGLASS = ITEMS.register("super_spyglass",
            () -> new SuperSpyglassItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}