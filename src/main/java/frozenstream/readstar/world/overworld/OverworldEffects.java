package frozenstream.readstar.world.overworld;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.Constants;
import frozenstream.readstar.data.meteor.MeteorManager;
import frozenstream.readstar.data.meteor.MeteorRenderer;
import frozenstream.readstar.events.FovEvent;
import frozenstream.readstar.data.planet.Planet;
import frozenstream.readstar.data.planet.PlanetManager;
import frozenstream.readstar.data.planet.PlanetRenderer;
import frozenstream.readstar.data.star.StarManager;
import frozenstream.readstar.data.star.StarRenderer;
import frozenstream.readstar.world.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

//TODO: 朝霞方向
//TODO: 取消行星白昼时眩光


public class OverworldEffects extends DimensionSpecialEffects {
    private VertexBuffer skyBuffer;
    private VertexBuffer darkBuffer;

    Minecraft minecraft = Minecraft.getInstance();

    public Matrix4f observeFromHere = new Matrix4f();


    public OverworldEffects() {
        super(192, true, SkyType.NORMAL, false, false);

        MeteorManager.init();
        createLightSky();
        createDarkSky();
    }

    private void createDarkSky() {
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }

        this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.darkBuffer.bind();
        this.darkBuffer.upload(buildSkyDisc(Tesselator.getInstance(), -16.0F));
        VertexBuffer.unbind();
    }

    private void createLightSky() {
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.skyBuffer.bind();
        this.skyBuffer.upload(buildSkyDisc(Tesselator.getInstance(), 16.0F));
        VertexBuffer.unbind();
    }

    private static MeshData buildSkyDisc(Tesselator tesselator, float y) {
        float f = Math.signum(y) * 512.0F;
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        bufferbuilder.addVertex(0.0F, y, 0.0F);

        for(int i = -180; i <= 180; i += 45) {
            bufferbuilder.addVertex(f * Mth.cos((float)i * 0.017453292F), y, 512.0F * Mth.sin((float)i * 0.017453292F));
        }

        return bufferbuilder.buildOrThrow();
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f frustumMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable skyFogSetup) {
        skyFogSetup.run();
        if (!isFoggy) {
            FogType fogtype = camera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !RenderUtil.doesMobEffectBlockSky(camera)) {
                PoseStack posestack = new PoseStack();
                posestack.mulPose(frustumMatrix);

                Vec3 vec3 = level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), partialTick);
                float f = (float)vec3.x;
                float f1 = (float)vec3.y;
                float f2 = (float)vec3.z;
                FogRenderer.levelFogColor();
                Tesselator tesselator = Tesselator.getInstance();
                RenderSystem.depthMask(false);
                RenderSystem.setShaderColor(f, f1, f2, 1.0F);
                ShaderInstance shaderinstance = RenderSystem.getShader();
                this.skyBuffer.bind();
                this.skyBuffer.drawWithShader(posestack.last().pose(), projectionMatrix, shaderinstance);
                VertexBuffer.unbind();
                RenderSystem.enableBlend();
                float[] afloat = level.effects().getSunriseColor(level.getTimeOfDay(partialTick), partialTick);
                float f11;
                float f7;
                float f8;
                float f9;
                if (afloat != null) {
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    posestack.pushPose();
                    posestack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                    f11 = Mth.sin(level.getSunAngle(partialTick)) < 0.0F ? 180.0F : 0.0F;
                    posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(f11));
                    posestack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
                    Matrix4f matrix4f = posestack.last().pose();
                    BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(afloat[0], afloat[1], afloat[2], afloat[3]);

                    for(int j = 0; j <= 16; ++j) {
                        f7 = (float)j * 6.2831855F / 16.0F;
                        f8 = Mth.sin(f7);
                        f9 = Mth.cos(f7);
                        bufferbuilder.addVertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).setColor(afloat[0], afloat[1], afloat[2], 0.0F);
                    }

                    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
                    posestack.popPose();
                }

                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                posestack.pushPose();
                f11 = 1.0F - level.getRainLevel(partialTick);

                Planet observer = PlanetManager.getPlanet("earth");
                observeFromHere = StarManager.observeFrom(observer, level.getDayTime() % 24000L);
                posestack.mulPose(observeFromHere);

                // 星体绘制
                PlanetRenderer.drawSun(tesselator, observer, posestack.last(), f11);
                FogRenderer.setupNoFog();
                PlanetRenderer.drawPlanets(tesselator, observer, posestack.last(), f11, RenderUtil.getStarBrightness(level, partialTick));

                MeteorRenderer.renderMeteors(tesselator, posestack.last());

                // 星光强度
                float starLight = Math.min(level.getStarBrightness(partialTick) * 2, f11);
                // 星星绘制
                if(camera.getEntity() instanceof Player player){
                    // 如果在使用原版望远镜 且FOV存在差值
                    if(Math.abs(FovEvent.fov - minecraft.options.fov().get()) > 32 && player.isScoping()){
                        Vector3f look = player.getViewVector(partialTick).toVector3f();
                        observeFromHere.transpose(new Matrix4f()).transformPosition(look);
                        float scaling = (float) (FovEvent.fov/minecraft.options.fov().get());
                        StarRenderer.RenderNearStars(posestack.last().pose(), projectionMatrix, look, 0.1f, scaling, starLight);
                    }
                    else StarRenderer.RenderStars(posestack.last().pose(), projectionMatrix, starLight);
                }
                skyFogSetup.run();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                posestack.popPose();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                double d0 = this.minecraft.player.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level);
                if (d0 < 0.0) {
                    posestack.pushPose();
                    posestack.translate(0.0F, 12.0F, 0.0F);
                    this.darkBuffer.bind();
                    this.darkBuffer.drawWithShader(posestack.last().pose(), projectionMatrix, shaderinstance);
                    VertexBuffer.unbind();
                    posestack.popPose();
                }
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.depthMask(true);
            }
        }
        return true; // 返回true禁用原版天空渲染
    }

    // 可选：覆盖其他方法
    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 color, float light) {
        return color.multiply(light * 0.94F + 0.06F, light * 0.94F + 0.06F, light * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

}