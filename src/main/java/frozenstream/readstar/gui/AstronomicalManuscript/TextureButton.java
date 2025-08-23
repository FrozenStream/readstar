package frozenstream.readstar.gui.AstronomicalManuscript;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

class TextureButton extends Button {
    private final ResourceLocation texture1;
    private final ResourceLocation texture2;

    public TextureButton(int x, int y, int width, int height,
                         ResourceLocation texture1, ResourceLocation texture2,
                         OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.texture1 = texture1;
        this.texture2 = texture2;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = texture1; // 默认纹理
        if (this.isHovered) texture = texture2; // 悬停纹理
        guiGraphics.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
    }
}