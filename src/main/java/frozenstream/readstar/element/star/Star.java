package frozenstream.readstar.element.star;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;

import java.util.List;

public record Star (
    String name,
    Vector3f position,
    int type,
    float Vmag
){
    public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Star::name),
            Codec.FLOAT.listOf().comapFlatMap(
                    list -> {
                        if (list.size() != 3) {
                            return DataResult.error(() -> "Vector3f requires exactly 3 floats");
                        }
                        return DataResult.success(new Vector3f(list.get(0), list.get(1), list.get(2)));
                    },
                    vec -> List.of(vec.x(), vec.y(), vec.z())
            ).fieldOf("position").forGetter(Star::position),
            Codec.INT.fieldOf("type").forGetter(Star::type),
            Codec.FLOAT.fieldOf("Vmag").forGetter(Star::Vmag)
    ).apply(instance, Star::new));

    // 构建 List<Star> 的 Codec
    public static final Codec<List<Star>> LIST_STAR_CODEC = CODEC.listOf();

}
