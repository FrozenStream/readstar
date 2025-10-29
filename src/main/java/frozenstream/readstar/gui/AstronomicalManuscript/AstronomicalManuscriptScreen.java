package frozenstream.readstar.gui.AstronomicalManuscript;

import frozenstream.readstar.Constants;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.data.planet.PlanetManager;
import frozenstream.readstar.gui.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.*;


@OnlyIn(Dist.CLIENT)
public class AstronomicalManuscriptScreen extends Screen {
    private static int BOOK_WIDTH;
    private static int BOOK_HEIGHT;

    private static int BOOK_X;
    private static int BOOK_Y;

    private static Planet Centre;
    private static final ArrayList<Planet> MainPlanets = new ArrayList<>();
    private static final Map<String, Double> MinDistanceMap = new HashMap<>();

    private int currentPage = 0;
    private int totalPages = 0;

    private static int MinScale = 1;
    private static int MaxScale = 8;
    private static final int MaxMinScale = 128;

    private TextureButton scaleUpButton;
    private TextureButton scaleDownButton;

    private TextureButton pageBackwardButton;
    private TextureButton pageForwardButton;

    private static final int ICON_SIZE = 10;
    private static final int HALF_ICON = ICON_SIZE / 2;

    private static final ResourceLocation Texture_Scale_Down_Button1 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_down1.png");
    private static final ResourceLocation Texture_Scale_Down_Button2 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_down2.png");
    private static final ResourceLocation Texture_Scale_Up_Button1 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_up1.png");
    private static final ResourceLocation Texture_Scale_Up_Button2 = ResourceLocation.fromNamespaceAndPath("readstar", "textures/gui/astronomical_manuscript/scale_up2.png");

    private static final ResourceLocation BOOK_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/book.png");
    private static final ResourceLocation PAGE_BACKWARD1 = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/page_backward.png");
    private static final ResourceLocation PAGE_BACKWARD2 = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/page_backward_highlighted.png");
    private static final ResourceLocation PAGE_FORWARD1 = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/page_forward.png");
    private static final ResourceLocation PAGE_FORWARD2 = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/page_forward_highlighted.png");

    public AstronomicalManuscriptScreen() {
        super(Component.translatable("screen.readstar.astronomical_manuscript"));

        // 按照距离升序排序主要行星
        MainPlanets.clear();
        for (Planet planet : PlanetManager.getPlanets()) {
            int level = PlanetManager.getPlanetsLevel(planet);
            if (level == 0 || level > 2) continue;
            MainPlanets.add(planet);
            totalPages++;
        }
        for (Planet planet : MainPlanets)
            Constants.LOG.info("Astronomical Manuscript have planet {}", planet.name);
    }

    @Override
    protected void init() {
        // 初始化尺寸
        BOOK_HEIGHT = this.height - 16;
        BOOK_WIDTH = BOOK_HEIGHT - 2;
        BOOK_X = (this.width - BOOK_WIDTH) / 2;
        BOOK_Y = (this.height - BOOK_HEIGHT) / 2;
        Constants.LOG.info("Astronomical Manuscript Screen Init, Book Size:{}x{}", BOOK_WIDTH, BOOK_HEIGHT);

        // 注册缩放按钮
        scaleUpButton = new TextureButton(
                BOOK_X + BOOK_WIDTH - 70, BOOK_Y + 100,
                12, 12, Texture_Scale_Up_Button1, Texture_Scale_Up_Button2,
                this::onScaleUpButton);
        scaleDownButton = new TextureButton(
                BOOK_X + BOOK_WIDTH - 70, BOOK_Y + 120,
                12, 12, Texture_Scale_Down_Button1, Texture_Scale_Down_Button2,
                this::onScaleDownButton);
        addRenderableWidget(scaleUpButton);
        addRenderableWidget(scaleDownButton);

        // 注册翻页按钮
        pageBackwardButton = new TextureButton(
                BOOK_X + BOOK_WIDTH - 100, BOOK_Y + BOOK_HEIGHT - 40,
                20, 10, PAGE_BACKWARD1, PAGE_BACKWARD2,
                this::onPageBackwardButton
        );
        pageForwardButton = new TextureButton(
                BOOK_X + BOOK_WIDTH - 80, BOOK_Y + BOOK_HEIGHT - 40,
                20, 10, PAGE_FORWARD1, PAGE_FORWARD2,
                this::onPageForwardButton
        );
        addRenderableWidget(pageBackwardButton);
        addRenderableWidget(pageForwardButton);

        pageBackwardButton.visible = false;
    }

    private double calMinDistance(Planet centre) {
        if (!MinDistanceMap.containsKey(centre.name)) {
            // 计算行星系最小距离
            double MinDistance = 1e20;
            for (Planet planet : centre.children) MinDistance = Math.min(MinDistance, planet.oribit.a());
            if (centre.oribit.a() != 0) MinDistance = Math.min(MinDistance, centre.oribit.a());

            MinDistance *= 0.5;
            MinDistanceMap.put(centre.name, MinDistance);
            Constants.LOG.info("Astronomical Manuscript Planet: {} have MinDistance:{}", centre.name, MinDistance);
        }
        return MinDistanceMap.get(centre.name);
    }

    private void onScaleUpButton(Button button) {
        if (MinScale <= MaxMinScale) {
            MinScale *= 4;
            MaxScale *= 4;
        }
    }

    private void onScaleDownButton(Button button) {
        if (MinScale > 1) {
            MinScale /= 4;
            MaxScale /= 4;
        }
    }

    private void onPageBackwardButton(Button button) {
        currentPage--;
        MinScale = 1;
        MaxScale = 8;
        pageForwardButton.visible = true;

        if (currentPage == 0) button.visible = false;
    }

    private void onPageForwardButton(Button button) {
        currentPage++;
        MinScale = 1;
        MaxScale = 8;
        pageBackwardButton.visible = true;

        if (currentPage == totalPages - 1) button.visible = false;
    }


    public void renderPage(GuiGraphics guiGraphics) {
        // 指定当前中心
        Centre = MainPlanets.get(currentPage);
        double MinDistance = calMinDistance(Centre);

        // 简介
        Component planetName = Component.translatable("planet.readstar." + Centre.name)
                .withStyle(style -> style.withBold(true));
        RenderUtil.drawCenteredString(guiGraphics, font, planetName, BOOK_X + BOOK_WIDTH / 2, BOOK_Y + 15, 0xFF000000, false);

        Component planetDesc = Component.translatable("planetdesc.readstar." + Centre.name.toLowerCase());
        String descText = planetDesc.getString();
        int maxWidth = BOOK_WIDTH - 90;
        int startX = BOOK_X + 45;
        int startY = BOOK_Y + 30;

        List<FormattedCharSequence> wrappedLines = font.split(Component.literal(descText), maxWidth);
        for (int i = 0; i < wrappedLines.size(); i++) {
            guiGraphics.drawString(font, wrappedLines.get(i), startX, startY + i * font.lineHeight, 0xFF000000, false);
        }

        // 绘制行星运行
        int centerX = BOOK_X + BOOK_WIDTH / 2;
        int centerY = BOOK_Y + BOOK_HEIGHT / 2;

        double Min = MinDistance * MinScale;
        double Max = MinDistance * MaxScale;

        renderPlanetInBook(guiGraphics, Centre, Min, Max, centerX, centerY);
        for (Planet planet : Centre.children)
            renderPlanetInBook(guiGraphics, planet, Min, Max, centerX, centerY);
        if (PlanetManager.getPlanetsLevel(Centre.parent) != 0)
            renderPlanetInBook(guiGraphics, Centre.parent, Min, Min, centerX, centerY);
    }


    public void renderPlanetInBook(GuiGraphics guiGraphics, Planet planet, double min, double max, int centerX, int centerY) {
        Vector2d vec2 = new Vector2d(
                planet.position.x - Centre.position.x,
                planet.position.z - Centre.position.z
        );

        double length = vec2.length();
        if (length < min) vec2.normalize(min);
        if (length > max) vec2.normalize(max);
        int planetX = centerX + (int) (vec2.x / max * BOOK_HEIGHT / 4);
        int planetY = centerY + (int) (vec2.y / max * BOOK_HEIGHT / 4);

        // 绘制行星图标
        ResourceLocation planetTexture = planet.getIcon();
        guiGraphics.blit(planetTexture, planetX - HALF_ICON, planetY - HALF_ICON, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderUtil.drawSmallString(guiGraphics, font, planet.name, planetX, planetY + HALF_ICON, 0xFF000000, false, 0.8f);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制书本背景和页码
        renderBookBackground(guiGraphics);
        renderPageNumbers(guiGraphics);

        renderPage(guiGraphics);
        scaleUpButton.render(guiGraphics, mouseX, mouseY, partialTick);
        scaleDownButton.render(guiGraphics, mouseX, mouseY, partialTick);
        pageBackwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
        pageForwardButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    private void renderBookBackground(GuiGraphics guiGraphics) {
        // 绘制书本整体背景
        guiGraphics.blit(BOOK_BACKGROUND, BOOK_X, BOOK_Y, 0, 0,
                BOOK_WIDTH, BOOK_HEIGHT,
                (int) (BOOK_WIDTH * 1.4), (int) (BOOK_HEIGHT * 1.36));
    }


    private void renderPageNumbers(GuiGraphics guiGraphics) {
        String PageStr = (currentPage + 1) + " / " + totalPages;
        RenderUtil.drawCenteredString(guiGraphics, font, PageStr, BOOK_X + BOOK_WIDTH / 2, BOOK_Y + BOOK_HEIGHT - 30, 0xFF000000, false);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
