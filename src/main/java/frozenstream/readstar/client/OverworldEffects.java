package frozenstream.readstar.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import frozenstream.readstar.data.Planet;
import frozenstream.readstar.data.PlanetManager;
import frozenstream.readstar.data.StarManager;
import frozenstream.readstar.events.FovEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public class OverworldEffects extends DimensionSpecialEffects {
    private VertexBuffer skyBuffer;
    private VertexBuffer starBuffer;
    private VertexBuffer darkBuffer;

    Minecraft minecraft = Minecraft.getInstance();

    public Matrix4f observeFromHere = new Matrix4f();


    public OverworldEffects() {
        super(192, true, SkyType.NORMAL, false, false);

        createStars();
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

    private void createStars() {
        if (this.starBuffer != null) {
            this.starBuffer.close();
        }

        this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.starBuffer.bind();
        this.starBuffer.upload(this.drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }

    private MeshData drawStars(Tesselator tesselator) {
        RandomSource randomsource = RandomSource.create(10842L);
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for(int j = 0; j < 1500; ++j) {
            float f1 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f2 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f3 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f4 = 0.15F + randomsource.nextFloat() * 0.1F;
            float f5 = Mth.lengthSquared(f1, f2, f3);
            if (!(f5 <= 0.010000001F) && !(f5 >= 1.0F)) {
                Vector3f vector3f = (new Vector3f(f1, f2, f3)).normalize(100.0F);
                float f6 = (float)(randomsource.nextDouble() * 3.1415927410125732 * 2.0);
                Quaternionf quaternionf = (new Quaternionf()).rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), vector3f).rotateZ(f6);
                bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, -f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(f4, f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, f4, 0.0F)).rotate(quaternionf)));
                bufferbuilder.addVertex(vector3f.add((new Vector3f(-f4, -f4, 0.0F)).rotate(quaternionf)));
            }
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
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);

                long time = level.getDayTime() % 24000L;

                Planet observer = PlanetManager.getPlanet("Earth");
                observeFromHere = StarManager.observeFrom(observer, time);
                posestack.mulPose(observeFromHere);

                // 星体绘制
                PlanetManager.drawPlanets(tesselator, observer, posestack.last(), time, f11);

                // 星星绘制
                float starLight = Math.min(level.getStarBrightness(partialTick) * 2, f11);
                FogRenderer.setupNoFog();
                if(camera.getEntity() instanceof Player player){
                    if(Math.abs(FovEvent.fov - minecraft.options.fov().get()) <= 8.0) StarManager.RenderStars(projectionMatrix, posestack.last().pose(), starLight);
                    else {
                        Vector3f look = player.getViewVector(partialTick).toVector3f();
                        observeFromHere.transpose(new Matrix4f()).transformPosition(look);
                        float scaling = (float) (FovEvent.fov/minecraft.options.fov().get());
                        StarManager.RenderNearStars(posestack.last(), look, 0.1f, 1);
                    }
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