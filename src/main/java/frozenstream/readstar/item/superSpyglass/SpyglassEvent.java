package frozenstream.readstar.item.superSpyglass;

import frozenstream.readstar.Constants;
import frozenstream.readstar.item.ModItems;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;


@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class SpyglassEvent {
    @SubscribeEvent
    public static void handeFOVModifier(ViewportEvent.ComputeFov event) {
        float targetFOV, currentFOV = ScopeManager.getFOVCurrent();
        if (!ScopeManager.isScoping()) targetFOV = ScopeManager.getFOVNormal();
        else targetFOV = ScopeManager.getFOVTarget();

        if(targetFOV != currentFOV) {
            float next = currentFOV, delta = targetFOV - currentFOV;
            next += delta * 0.2f;
            if(Math.abs(delta) < 0.1f) next = targetFOV;
            ScopeManager.updateFOVCurrent(next);
        }

        Entity entity = event.getCamera().getEntity();
        if (entity instanceof Player player) {
            if (player.getMainHandItem().is(ModItems.SUPER_SPYGLASS)) {
                event.setFOV(ScopeManager.getFOVCurrent()); // 设置新的视野值
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderGuiEvent.Post event) {
        if (ScopeManager.isScoping()) {
            ScopeOverlayRenderer.render(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (ScopeManager.isScoping()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientInitialized(ClientPlayerNetworkEvent.LoggingIn event) {
        ScopeManager.askFOVNormal(); // 在客户端初始化时更新 NormalFOV
    }

    @SubscribeEvent
    public static void onSettingsChanged(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof OptionsScreen) {
            ScopeManager.askFOVNormal(); // 在设置关闭时更新 NormalFOV
        }
    }
}
