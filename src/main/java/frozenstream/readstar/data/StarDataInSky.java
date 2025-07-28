package frozenstream.readstar.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record StarDataInSky(
        float x,
        float y,
        float z,
        boolean isWatching,
        ResourceLocation  texture
) {
}
