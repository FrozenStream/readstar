package frozenstream.readstar.mixin;

import frozenstream.readstar.Constants;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FogRenderer.class)
public class FogRendererModifyVectorMixin {

    @ModifyVariable(
            method = "setupColor",
            at = @At(value = "STORE"),
            name = "afloat"
    )
    private static float[] modifyFogColorVector(float[] original) {
        return null;
    }
}
