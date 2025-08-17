package frozenstream.readstar.gui.AstronomicalManuscript;

import com.mojang.blaze3d.vertex.PoseStack;
import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.data.planet.PlanetManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AstronomicalManuscriptScreen extends Screen {
    private static final int BOOK_WIDTH = 256;
    private static final int BOOK_HEIGHT = 180;
    private static final int PAGE_WIDTH = 120;
    private static final int PAGE_HEIGHT = 150;

    private static Planet MainCentre;
    private static Planet Centre;

    private static double MinDistance = 1e20;

    private static int MinScale = 1;
    private static int MaxScale = 8;

    private static int MaxMinScale = 128;

    private TextureButton scaleUpButton;
    private Button scaleDownButton;

    private static final int ICON_SIZE = 6;
    private static final int HALF_ICON = ICON_SIZE / 2;

    private final List<String> pages = new ArrayList<>();
    private int currentPage = 0;
    private int totalPages = 0;

    private static final ResourceLocation Texture_Scale_Down_Button1 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_down1.png");
    private static final ResourceLocation Texture_Scale_Down_Button2 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_down2.png");
    private static final ResourceLocation Texture_Scale_Up_Button1 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_up1.png");
    private static final ResourceLocation Texture_Scale_Up_Button2 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_up2.png");

    public AstronomicalManuscriptScreen() {
        super(Component.translatable("screen.readstar.astronomical_manuscript"));
        initPages();
    }

    private void initPages() {
        pages.add("天文观测手稿\n\n欢迎使用天文观测手稿！\n\n本手稿将为您展示太阳系中主要天体的运行轨道和相关信息。\n\n请翻页查看详细内容。");
        totalPages = pages.size() + 2;
    }

    @Override
    protected void init() {
        MainCentre = PlanetManager.SUN;
        Centre = MainCentre;

        calMinDistance();
        Constants.LOG.info("Astronomical Manuscript Screen Init, MinDistance:{}", MinDistance);

        // 注册缩放按钮
        scaleUpButton = new TextureButton(
                (this.width - BOOK_WIDTH) / 2 + BOOK_WIDTH - 40, (this.height - BOOK_HEIGHT) / 2 + 100,
                8, 8, Texture_Scale_Up_Button1, Texture_Scale_Up_Button2,
                this::onScaleUpButton);
        scaleDownButton = new TextureButton(
                (this.width - BOOK_WIDTH) / 2 + BOOK_WIDTH - 30, (this.height - BOOK_HEIGHT) / 2 + 100,
                8, 8, Texture_Scale_Down_Button1, Texture_Scale_Down_Button2,
                this::onScaleDownButton);
        addRenderableWidget(scaleUpButton);
        addRenderableWidget(scaleDownButton);
    }

    private void calMinDistance(){
        // 计算行星系最小距离
        MinDistance = 1e20;
        for (Planet planet : PlanetManager.getPlanets()) {
            if (planet.parent != Centre) continue;
            MinDistance = Math.min(MinDistance, planet.oribit.a());
        }
        MinDistance *= 0.5;
    }

    private void onScaleUpButton(Button button) {
        if(MinScale <= MaxMinScale) {
            MinScale *= 4;
            MaxScale *= 4;
        }
    }

    private void onScaleDownButton(Button button) {
        if(MinScale > 1) {
            MinScale /= 4;
            MaxScale /= 4;
        }
    }

    public void drawCenteredString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, dropShadow);
    }

    public void drawSmallString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean dropShadow, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x - font.width(text) * scale / 2f, y, 0); // 将原点移动到绘制位置
        poseStack.scale(scale, scale, scale);
        // 在缩放后的坐标系中绘制文本（注意坐标需要相应调整）
        guiGraphics.drawString(font, text, 0, 0, color, dropShadow);
        poseStack.popPose(); // 恢复原始变换
    }



    public void renderTitlePage(GuiGraphics guiGraphics, int bookX, int bookY) {
        int centerX = bookX + BOOK_WIDTH / 2;
        int centerY = bookY + BOOK_HEIGHT / 2;
        if (!PlanetManager.star_prepared) return;

        double Min = MinDistance * MinScale;
        double Max = MinDistance * MaxScale;

        for (Planet planet : PlanetManager.getPlanets()) {
            if (planet == Centre) renderPlanetInBook(guiGraphics, planet, Min, Max, centerX, centerY);
            else if (planet.parent == Centre) renderPlanetInBook(guiGraphics, planet, Min, Max, centerX, centerY);
        }
    }


    public void renderPlanetInBook(GuiGraphics guiGraphics, Planet planet, double min, double max, int centerX, int centerY) {
        Vector2d vec2 = new Vector2d(
                planet.position.x - Centre.position.x,
                planet.position.z - Centre.position.z
        );

        double length = vec2.length();
        if(length < min)vec2.normalize(min);
        if(length > max)vec2.normalize(max);
        int planetX = centerX + (int) (vec2.x / max * BOOK_HEIGHT / 3);
        int planetY = centerY + (int) (vec2.y / max * BOOK_HEIGHT / 3);

        // 绘制行星图标
        ResourceLocation planetTexture = getPlanetIcon(planet.name);
        guiGraphics.blit(planetTexture, planetX - HALF_ICON, planetY - HALF_ICON,
                0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);


        drawSmallString(guiGraphics, font, planet.name, planetX, planetY + HALF_ICON,
                0xFF000000, false, 0.4f);
    }

    public void renderPlanetPage(GuiGraphics guiGraphics, Planet planet, int bookX, int bookY) {


    }

    private ResourceLocation getPlanetIcon(String planetName) {
        // 将行星名称映射到对应的图标文件
        return switch (planetName.toLowerCase()) {
            case "sun" -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/sun.png");
            case "earth" -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/earth.png");
            case "mars" -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/mars.png");
            case "venus" -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/venus.png");
            case "moon" -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/moon.png");
            default -> ResourceLocation.fromNamespaceAndPath("readstar", "textures/icons/sun.png");
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 计算书本位置
        int bookX = (this.width - BOOK_WIDTH) / 2;
        int bookY = (this.height - BOOK_HEIGHT) / 2;

        // 绘制书本背景
        renderBookBackground(guiGraphics, bookX, bookY);

        if(currentPage == 0){
            renderTitlePage(guiGraphics, bookX, bookY);
            scaleUpButton.render(guiGraphics, mouseX, mouseY, partialTick);
            scaleDownButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        else{
            // 绘制左侧页面内容
            renderLeftPage(guiGraphics, bookX, bookY);
            // 绘制右侧页面内容
            renderRightPage(guiGraphics, bookX, bookY);
            // 绘制装订线
            guiGraphics.fill(bookX + BOOK_WIDTH/2 - 1, bookY + 10, bookX + BOOK_WIDTH/2 + 1, bookY + BOOK_HEIGHT - 10, 0xFF444444);
        }

        // 绘制页码
        renderPageNumbers(guiGraphics, bookX, bookY);
        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, width / 2, bookY - 15, 0xFFFFFF);
    }


    private void renderBookBackground(GuiGraphics guiGraphics, int bookX, int bookY) {
        // 绘制书本整体背景
        guiGraphics.fill(bookX, bookY, bookX + BOOK_WIDTH, bookY + BOOK_HEIGHT, 0xFF8B4513); // 棕色书皮

        // 绘制页面背景
        guiGraphics.fill(bookX + 5, bookY + 5, bookX + BOOK_WIDTH - 5, bookY + BOOK_HEIGHT - 5, 0xFFF5F5DC); // 米黄色页面

        // 绘制书边装饰
        guiGraphics.fill(bookX, bookY, bookX + BOOK_WIDTH, bookY + 3, 0xFF5D4037); // 书顶边
        guiGraphics.fill(bookX, bookY + BOOK_HEIGHT - 3, bookX + BOOK_WIDTH, bookY + BOOK_HEIGHT, 0xFF5D4037); // 书底边
        guiGraphics.fill(bookX, bookY, bookX + 3, bookY + BOOK_HEIGHT, 0xFF5D4037); // 书左边缘
        guiGraphics.fill(bookX + BOOK_WIDTH - 3, bookY, bookX + BOOK_WIDTH, bookY + BOOK_HEIGHT, 0xFF5D4037); // 书右边缘
    }

    private void renderLeftPage(GuiGraphics guiGraphics, int bookX, int bookY) {
        int pageX = bookX + 15;
        int pageY = bookY + 15;

        if (currentPage < pages.size()) {
            // 绘制左侧页面文本
            drawStringWrapped(guiGraphics, pages.get(currentPage), pageX, pageY, PAGE_WIDTH - 10, 0xFF000000);
        }
    }

    private void renderRightPage(GuiGraphics guiGraphics, int bookX, int bookY) {
        int pageX = bookX + BOOK_WIDTH/2 + 10;
        int pageY = bookY + 15;

        if (currentPage + 1 < pages.size()) {
            // 绘制右侧页面文本
            drawStringWrapped(guiGraphics, pages.get(currentPage + 1), pageX, pageY, PAGE_WIDTH - 10, 0xFF000000);
        }
    }

    private void renderPageNumbers(GuiGraphics guiGraphics, int bookX, int bookY) {
        // 左页页码
        String leftPageNumber = String.valueOf(currentPage + 1);
        drawCenteredString(guiGraphics, font, leftPageNumber, bookX + BOOK_WIDTH/4, bookY + BOOK_HEIGHT - 15, 0xFF000000, false);
        // 右页页码
        if (currentPage + 1 < totalPages) {
            String rightPageNumber = String.valueOf(currentPage + 2);
            drawCenteredString(guiGraphics ,font, rightPageNumber, bookX + BOOK_WIDTH*3/4, bookY + BOOK_HEIGHT - 15, 0xFF000000, false);
        }
        // 总页数
        String totalPagesStr = "/ " + totalPages;
        drawCenteredString(guiGraphics, font, totalPagesStr, bookX + BOOK_WIDTH/2, bookY + BOOK_HEIGHT - 15, 0xFF000000, false);
    }

    private void drawStringWrapped(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        String[] lines = text.split("\n");
        int currentY = y;

        for (String line : lines) {
            if (line.isEmpty()) {
                currentY += font.lineHeight + 2;
                continue;
            }

            // 处理长行的换行
            List<FormattedCharSequence> wrappedLines = font.split(Component.literal(line), maxWidth);

            for (FormattedCharSequence wrappedLine : wrappedLines) {
                guiGraphics.drawString(font, wrappedLine, x, currentY, color, false);
                currentY += font.lineHeight + 2;
            }
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
