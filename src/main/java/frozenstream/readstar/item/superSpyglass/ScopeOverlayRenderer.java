package frozenstream.readstar.item.superSpyglass;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScopeOverlayRenderer {

    private static final ResourceLocation SCOPE_MASK = ResourceLocation.withDefaultNamespace("textures/misc/spyglass_scope.png");

    public static void render(GuiGraphics guiGraphics) {
        if (!ScopeManager.isScoping()) return;

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();



        int f = Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
        int k = (guiGraphics.guiWidth() - f) / 2;
        int l = (guiGraphics.guiHeight() - f) / 2;
        int i1 = k + f;
        int j1 = l + f;
        RenderSystem.enableBlend();
        guiGraphics.blit(SCOPE_MASK, k, l, -90, 0.0F, 0.0F, f, f, f, f);
        RenderSystem.disableBlend();
        guiGraphics.fill(RenderType.guiOverlay(), 0, j1, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, 0, guiGraphics.guiWidth(), l, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), i1, l, guiGraphics.guiWidth(), j1, -90, -16777216);
    }
}