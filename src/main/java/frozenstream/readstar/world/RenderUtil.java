package frozenstream.readstar.world;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public class RenderUtil {
    public static boolean doesMobEffectBlockSky(Camera camera) {
        Entity var3 = camera.getEntity();
        boolean effected;
        if (var3 instanceof LivingEntity livingentity) {
            effected = livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
        } else {
            effected = false;
        }
        return effected;
    }

    public static float getStarBrightness(Level level, float partialTick) {
        float f = level.getTimeOfDay(partialTick);
        float f1 = 1.0F - (Mth.cos(f * 6.2831855F) * 2.0F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        return f1;
    }
}
