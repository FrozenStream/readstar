package frozenstream.readstar.element.meteor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MeteorRenderer {
    private static final Vector3f OriVec = new Vector3f(0.0F, 0.0F, -1.0F);
    private static final Vector3f positionVec = new Vector3f();
    private static final Quaternionf quaternionf = new Quaternionf();
    private static final Vector3f tailVec = new Vector3f();
    private static final Vector3f perpendicularVec = new Vector3f();

    private static final Vector3f[] v = new Vector3f[4];

    private static final float meteorSize = 0.1f;
    private static final float tailWidth = 0.05f;

    static {
        v[0] = new Vector3f();
        v[1] = new Vector3f();
        v[2] = new Vector3f();
        v[3] = new Vector3f();
    }

    public static void renderMeteors(Tesselator tesselator, PoseStack.Pose pose) {
        MeteorManager.update();
        if (MeteorManager.meteors.isEmpty()) return;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Meteor meteor : MeteorManager.meteors) {
            // 获取流星当前位置
            meteor.getPosition(positionVec);
            quaternionf.identity().rotateTo(OriVec, positionVec);

            v[0].set(meteorSize, -meteorSize, 0.0F).rotate(quaternionf).add(positionVec);
            v[1].set(meteorSize, meteorSize, 0.0F).rotate(quaternionf).add(positionVec);
            v[2].set(-meteorSize, meteorSize, 0.0F).rotate(quaternionf).add(positionVec);
            v[3].set(-meteorSize, -meteorSize, 0.0F).rotate(quaternionf).add(positionVec);

            // 获取流星颜色
            Vector3f color = meteor.color;

            // 绘制流星主体（较大的点）
            bufferBuilder.addVertex(pose, v[0]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[1]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[2]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[3]).setColor(color.x, color.y, color.z, 1.0f);

            // 绘制流星尾巴
            perpendicularVec.set(positionVec).cross(meteor.direction).normalize();
            tailVec.set(meteor.direction).mul(-meteor.currentLength*0.4f).add(positionVec);

            v[0].set(perpendicularVec).mul(tailWidth).add(positionVec);
            v[1].set(perpendicularVec).mul(-tailWidth).add(positionVec);
            v[2].set(perpendicularVec).mul(-tailWidth).add(tailVec);
            v[3].set(perpendicularVec).mul(tailWidth).add(tailVec);

            bufferBuilder.addVertex(pose, v[0]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[1]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[2]).setColor(color.x, color.y, color.z, 1.0f);
            bufferBuilder.addVertex(pose, v[3]).setColor(color.x, color.y, color.z, 1.0f);
        }
        // 提交绘制
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}
