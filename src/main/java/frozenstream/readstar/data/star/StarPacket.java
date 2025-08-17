package frozenstream.readstar.data.star;

import net.minecraft.world.phys.Vec3;

public record StarPacket(
        String name,
        String description,
        Vec3 position,
        int type
) {

}
