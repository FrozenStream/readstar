package frozenstream.readstar.data.star;

import org.joml.Vector3f;

public record Star (
    String name,
    String description,
    Vector3f position,
    int type
){

}
