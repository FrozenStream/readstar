package frozenstream.readstar.item.vanilla_spyglass;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class VanillaSpyglassManager {
    private static float FOV_TARGET = 20f;
    private static float FOV_CURRENT = 70f;
    private static float FOV_NORMAL = 70f;
    private static float FOV_SCROLL_SPEED = 2.0f;

    public static void askFOVNormal() {
        FOV_NORMAL = Minecraft.getInstance().options.fov().get().floatValue();
    }

    public static float getFOVNormal() {
        return FOV_NORMAL;
    }

    public static void updateFOVCurrent(float FOV) {
        FOV_CURRENT = FOV;
    }

    public static float getFOVCurrent() {
        return FOV_CURRENT;
    }

    public static void updateFOVTarget(float FOV) {
        FOV_TARGET = Mth.clamp(FOV,10,FOV_NORMAL);
    }

    public static float getFOVTarget() {
        return FOV_TARGET;
    }

    public static float getFOVScrollSpeed() {
        return FOV_SCROLL_SPEED;
    }

    public static void setFOVScrollSpeed(float speed) {
        FOV_SCROLL_SPEED = speed;
    }
}
