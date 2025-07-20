package frozenstream.readstar.item.vanilla_spyglass;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;


@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class VanillaSpyglassEvent {

    /**
     * 接管原版望远镜缩放事件
     * */
    @SubscribeEvent
    public static void handeFOVModifier(ViewportEvent.ComputeFov event) {
        float targetFOV = 70.f, currentFOV = VanillaSpyglassManager.getFOVCurrent();

        Entity entity = event.getCamera().getEntity();
        if (entity instanceof Player player) {
            if (!player.isScoping()) targetFOV = VanillaSpyglassManager.getFOVNormal();
            else targetFOV = VanillaSpyglassManager.getFOVTarget();
        }

        if(targetFOV != currentFOV) {
            float next = currentFOV, delta = targetFOV - currentFOV;
            next += delta * 0.2f;
            if(Math.abs(delta) < 0.1f) next = targetFOV;
            VanillaSpyglassManager.updateFOVCurrent(next);
        }

        event.setFOV(VanillaSpyglassManager.getFOVCurrent()); // 设置新的视野值
    }


    /***
     * 滚轮修改缩放倍率
     */
    @SubscribeEvent
    public static void handleMouseScroll(InputEvent.MouseScrollingEvent event) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity instanceof Player player) {
            if (player.isScoping()) {
                float scrollDelta = (float) event.getScrollDeltaY() * VanillaSpyglassManager.getFOVScrollSpeed();
                float currentTarget = VanillaSpyglassManager.getFOVTarget();
                VanillaSpyglassManager.updateFOVTarget(currentTarget + scrollDelta);

                event.setCanceled(true);
            }
        }
    }

    //TODO: 视角移动速度和缩放倍率相关



    /**
     * 在客户端初始化时更新 NormalFOV
     */
    @SubscribeEvent
    public static void onClientInitialized(ClientPlayerNetworkEvent.LoggingIn event) {
        VanillaSpyglassManager.askFOVNormal();
    }

    /**
     * 在设置界面关闭时更新 NormalFOV
     */
    @SubscribeEvent
    public static void onSettingsChanged(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof OptionsScreen) {
            VanillaSpyglassManager.askFOVNormal(); // 在设置关闭时更新 NormalFOV
        }
    }
}
