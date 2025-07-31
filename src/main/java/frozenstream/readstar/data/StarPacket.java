package frozenstream.readstar.data;

import net.minecraft.world.phys.Vec3;

public record StarPacket(
        String name,
        String description,
        Vec3 position
) {

}
