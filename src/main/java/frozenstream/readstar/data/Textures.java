package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2f;


public class Textures {


    public static void getUV(int num, Vector2f[] in) {
        int i = num % 4;
        int j = num / 4;
        float l = i / 4.0F;
        float t = j / 2.0F;
        float r = (i + 1) / 4.0F;
        float b = (j + 1) / 2.0F;
        in[0].set(r, b);
        in[1].set(l, b);
        in[2].set(l, t);
        in[3].set(r, t);
    }

    public static void getUV(int width, int height, int num, Vector2f[] in) {
        float i = num % width;
        float j = num / width;
        float l = i / width;
        float t = j / height;
        float r = (i + 1) / width;
        float b = (j + 1) / height;
        in[0].set(r, b);
        in[1].set(l, b);
        in[2].set(l, t);
        in[3].set(r, t);
    }
}
