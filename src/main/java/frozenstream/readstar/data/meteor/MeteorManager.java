package frozenstream.readstar.data.meteor;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import java.util.LinkedList;
import java.util.List;


public class MeteorManager {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final RandomSource randomsource = RandomSource.create(10842L);
    public static List<Meteor> meteors = new LinkedList<>();

    public static void init() {
        meteors.clear();
    }

    public static void addMeteor(Vector3f startPosition, Vector3f direction, Vector3f color, float pathLength, float speed) {
        if(meteors.size() >= 100) return;
        Constants.LOG.info("Generated a random meteor {} {}.", startPosition, direction);
        Meteor meteor = new Meteor();
        meteor.startPosition = startPosition;
        meteor.direction = direction;
        meteor.color = color;
        meteor.pathLength = pathLength;
        meteor.currentLength = 0;
        meteor.speed = speed;
        meteors.add(meteor);
    }

    public static void update() {
        if (minecraft.level == null) return;
        meteors.removeIf(Meteor::step);
    }
}
