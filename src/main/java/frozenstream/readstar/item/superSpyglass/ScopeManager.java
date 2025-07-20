package frozenstream.readstar.item.superSpyglass;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class ScopeManager {
    private static boolean isScoping = false;
    private static float FOV_TARGET = 20f;
    private static float FOV_CURRENT = 70f;
    private static float FOV_NORMAL = 70f;

    public static void askFOVNormal() {
        FOV_NORMAL = Minecraft.getInstance().options.fov().get().floatValue();
    }

    public static float getFOVNormal() {
        return FOV_NORMAL;
    }

    public static void setScoping(boolean isScoping) {
        ScopeManager.isScoping = isScoping;
    }

    public static boolean isScoping() {
        return isScoping;
    }

    public static void updateFOVCurrent(float FOV) {
        FOV_CURRENT = FOV;
    }

    public static float getFOVCurrent() {
        return FOV_CURRENT;
    }

    public static void updateFOVTarget(float FOV) {
        FOV_TARGET = FOV;
    }

    public static float getFOVTarget() {
        return FOV_TARGET;
    }
}
