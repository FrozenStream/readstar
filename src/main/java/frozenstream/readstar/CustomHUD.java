package frozenstream.readstar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class CustomHUD {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 在屏幕左上角绘制文本
        guiGraphics.drawString(
                mc.font,
                Component.literal("这是一个简单的HUD示例"),
                10, 10,
                0xFFFFFF,
                true
        );

        // 显示玩家坐标
        String coords = String.format("X: %.2f, Y: %.2f, Z: %.2f",
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ());

        guiGraphics.drawString(
                mc.font,
                Component.literal(coords),
                10, 25,
                0xFFFF00,
                true
        );

        // 绘制一个简单的矩形框
        guiGraphics.fill(8, 8, 200, 40, 0x80000000); // 半透明黑色背景
    }
}
