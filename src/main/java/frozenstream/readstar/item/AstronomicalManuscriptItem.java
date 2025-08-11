package frozenstream.readstar.item;

import frozenstream.readstar.gui.AstronomicalManuscript.AstronomicalManuscriptScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AstronomicalManuscriptItem extends Item {
    public AstronomicalManuscriptItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) {
            openAstronomicalScreen();
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @OnlyIn(Dist.CLIENT)
    private void openAstronomicalScreen() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new AstronomicalManuscriptScreen());
    }
}
