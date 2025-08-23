package frozenstream.readstar.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RenderUtil {
    static public void drawCenteredString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, dropShadow);
    }

    static public void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean dropShadow) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, dropShadow);
    }

    static public void drawSmallString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean dropShadow, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x - font.width(text) * scale / 2f, y, 0); // 将原点移动到绘制位置
        poseStack.scale(scale, scale, scale);
        // 在缩放后的坐标系中绘制文本（注意坐标需要相应调整）
        guiGraphics.drawString(font, text, 0, 0, color, dropShadow);
        poseStack.popPose(); // 恢复原始变换
    }
}