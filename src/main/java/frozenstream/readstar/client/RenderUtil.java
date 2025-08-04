package frozenstream.readstar.client;

import net.minecraft.client.Camera;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ViewportEvent;

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

    public static void doesMobEffectBlockSky(ViewportEvent.RenderFog event) {

    }


}
