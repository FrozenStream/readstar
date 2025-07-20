package frozenstream.readstar.client;

import frozenstream.readstar.Constants;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class StarRender {

    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/star1.png");
    static Matrix4f originMatrix;
    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        // NeoForge 的渲染阶段名称可能有变化
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (level == null) return;
        if (player == null) return;

        boolean isUsingSpyglass = player.getUseItem().is(Items.SPYGLASS) && player.getUseItemRemainingTicks() > 0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, STAR_TEXTURE);

        // 绘制星星
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Quaternionf viewRotation = event.getCamera().rotation();
        viewRotation.invert(); // 获取视图矩阵的逆矩阵
        poseStack.mulPose(viewRotation);
        originMatrix = poseStack.last().pose();
        poseStack.popPose();


//        // 1. 获取玩家视线方向向量（标准化后的前向向量）
//        Quaternionf viewQuat = event.getCamera().rotation();
//        Vector3f lookVec = new Vector3f(0, 0, -1); // 默认前向向量
//        lookVec.rotate(viewQuat); // 应用摄像机旋转
//        // 2. 计算星星相对玩家的方向向量（星星位于摄像机前100单位）
//        Vector3f starPos = new Vector3f(0, 0, -100); // 与渲染位置一致
//        Vector3f toStar = starPos.normalize();
//        // 3. 计算点积判断方向一致性
//        float dot = lookVec.dot(toStar);
//        boolean isLooking = dot > 0.99999f; // 阈值可根据需求调整
//        // 4. 可选：输出调试信息
          var f = 2f;
//        if (isLooking) {
//            ExampleMod.LOGGER.info("Player is looking at the star!");
//            if (isUsingSpyglass) f = f*1.2f;
//        }

        // 定义时间段（单位：游戏刻）
        float alpha = getAlpha(level);

        RenderStar( f, alpha, 45, 0, 45);
        RenderStar( f, alpha, 30, 60, 27);
        RenderStar( f, alpha, 34, 2, 24);
        RenderStar( f, alpha, 45, 11, 64);
        RenderStar( f, alpha, 64, 22, 64);
        RenderStar( f, alpha, 33, 5, 2);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();


    }

    private static void RenderStar(float size, float alpha, float XRot, float YRot, float ZRot) {
        Matrix4f tmpMatrix = new Matrix4f(originMatrix);

        tmpMatrix.rotate(Axis.YP.rotationDegrees(YRot));
        tmpMatrix.rotate(Axis.XP.rotationDegrees(XRot));
        tmpMatrix.rotate(Axis.ZP.rotationDegrees(ZRot));
        tmpMatrix.translate(0, 0, -100);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(tmpMatrix, -size, -size, 0).setUv(0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, size, -size, 0).setUv(1, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, size, size, 0).setUv(1, 1).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, -size, size, 0).setUv(0, 1).setColor(1.0f, 1.0f, 1.0f, alpha);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    private static float getAlpha(@NotNull Level level) {
        final int SUNRISE_START = 23000;  // 日出开始
        final int SUNRISE_END = 24000;    // 日出结束（次日0点）
        final int DAY_START = 0;          // 白天开始
        final int DAY_END = 12000;        // 白天结束
        final int SUNSET_START = 12000;   // 日落开始
        final int SUNSET_END = 13000;     // 日落结束
        final int NIGHT_START = 13000;    // 夜晚开始
        final int NIGHT_END = 23000;      // 夜晚结束
        float alpha = 0f;
        float alpha_max = 1f, alpha_min = 0.2f;
        var time = level.getDayTime() % 24000;
        float tmp;
        //日出渐变
        if ((tmp = whereInTimeRange(time, SUNRISE_START, SUNRISE_END)) != -1)
            alpha = alpha_max - (alpha_max - alpha_min) * tmp;
        else if (whereInTimeRange(time, DAY_START, DAY_END) != -1)
            alpha = alpha_min;
        else if ((tmp = whereInTimeRange(time, SUNSET_START, SUNSET_END)) != -1)
            alpha = (alpha_max - alpha_min) * tmp + alpha_min;
        else if (whereInTimeRange(time, NIGHT_START, NIGHT_END) != -1)
            alpha = alpha_max;
        return alpha;
    }

    private static float whereInTimeRange(long value, long min, long max) {
        if(min<max) {
            if(value>=min && value<=max) return (float) (value - min) / (max - min);
            return -1;
        }
        else {
            long length = max - min + (long) 24000;
            if(value<min && value>max)return -1;
            if(value>=min)return (float) (value - min) / length;
            else return (float) (value + (long) 24000 -min) / length;
        }
    }
}
