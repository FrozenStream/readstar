package frozenstream.readstar.client;

import frozenstream.readstar.Constants;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.data.PlanetManager;
import frozenstream.readstar.data.StarDataInSky;
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
import org.joml.Vector3f;

import java.util.ArrayList;


@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class StarRender {

    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");

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
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

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
//
//        StarManager.isWatching(lookVec);

        // 定义时间段（单位：游戏刻）
        float alpha = getAlpha(level);

        ArrayList<StarDataInSky> starpos = PlanetManager.getInSky("Earth");

        if(starpos.size() > 1) {
            StarDataInSky star = starpos.get(0);
            RenderStar(20, 1f, rotateTo(star.x(), star.y(), star.z()), MOON_LOCATION);
            star = starpos.get(1);
            RenderStar(20, 1f, rotateTo(star.x(), star.y(), star.z()), SUN_LOCATION);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();


    }

    private static void RenderStar(float size, float alpha, Matrix4f Matrix, ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f tmpMatrix = new Matrix4f(originMatrix);
        tmpMatrix.mul(Matrix);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(tmpMatrix, size, size, 100).setUv(1, 1).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, size, -size, 100).setUv(0, 1).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, -size, -size, 100).setUv(0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
        bufferBuilder.addVertex(tmpMatrix, -size, size, 100).setUv(1, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
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

    public static Matrix4f rotateTo(float x, float y, float z) {
        // 归一化目标向量
        Vector3f v2 = new Vector3f(x, y, z).normalize();
        // 原始向量 (0, 0, -1)
        Vector3f v1 = new Vector3f(0, 0, 1);

        // 计算旋转轴
        Vector3f axis = new Vector3f();
        v1.cross(v2, axis);

        // 计算旋转角度的余弦值
        float cosTheta = v1.dot(v2);

        // 如果向量已经对齐，直接返回单位矩阵
        if (cosTheta >= 1.0f - 1e-6f) {
            return new Matrix4f();
        }

        // 如果向量是反向的，绕任意垂直轴旋转180度
        if (cosTheta <= -1.0f + 1e-6f) {
            Vector3f perpendicularAxis = new Vector3f(1, 0, 0);
            return new Matrix4f().rotate((float) Math.PI, perpendicularAxis);
        }

        // 计算旋转轴的归一化向量
        axis.normalize();

        // 使用Quaternionf构造旋转矩阵
        Quaternionf rotationQuaternion = new Quaternionf();
        rotationQuaternion.setAngleAxis((float) Math.acos(cosTheta), axis.x, axis.y, axis.z);

        // 将四元数转换为旋转矩阵
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.rotation(rotationQuaternion);

        return rotationMatrix;
    }
}
