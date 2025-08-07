package frozenstream.readstar.item.vanilla_spyglass;

import frozenstream.readstar.Constants;
import frozenstream.readstar.client.OverworldEffects;
import frozenstream.readstar.data.Star;
import frozenstream.readstar.data.StarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;


@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class VanillaSpyglassEvent {

    @SubscribeEvent
    public static void onRenderWorld(RenderGuiEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.isScoping()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                return;
            }

            DimensionSpecialEffects effects = mc.level.effects();
            if(effects instanceof OverworldEffects overworldEffects){
                Vector3f lookAt = player.getViewVector(1f).toVector3f();
                overworldEffects.observeFromHere.transpose(new Matrix4f()).transformPosition(lookAt);

                Star starLookAt = StarManager.lookingAt(lookAt, 0.01f);

                if(starLookAt == null) return;


                GuiGraphics guiGraphics = event.getGuiGraphics();
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();


                String starName = String.format("name: %s", starLookAt.name());
                String starDesc = String.format("description: %s",starLookAt.description());
                // 在屏幕左上角绘制文本
                guiGraphics.drawString(
                        mc.font,
                        Component.literal(starName),
                        10, 10,
                        0xFFFFFF,
                        true
                );

                guiGraphics.drawString(
                        mc.font,
                        Component.literal(starDesc),
                        10, 25,
                        0xFFFF00,
                        true
                );

                //显示玩家坐标
                String coords = String.format("X: %.2f, Y: %.2f, Z: %.2f", lookAt.x, lookAt.y, lookAt.z);

                guiGraphics.drawString(
                        mc.font,
                        Component.literal(coords),
                        10, 40,
                        0xFFFF00,
                        true
                );

                // 绘制一个简单的矩形框
                guiGraphics.fill(8, 8, 200, 55, 0x80000000); // 半透明黑色背景
            }



        }
    }
}
