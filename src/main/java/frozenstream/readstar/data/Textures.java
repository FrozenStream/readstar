package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Textures {
    private static final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");

    public static ResourceLocation getTexture(String name) {
        String name_lower = name.toLowerCase();
        if (name_lower.equals("moon")) return MOON_LOCATION;
        if (name_lower.equals("sun")) return SUN_LOCATION;
        String texture_path = "textures/environment/" + name_lower + ".png";
        ResourceLocation customTexture = ResourceLocation.fromNamespaceAndPath("readstar", texture_path);
        try {
            resourceManager.getResource(customTexture);
            return customTexture;
        } catch (Exception e) {
            Constants.LOG.warn("No texture for {}", name);
            return null;
        }
    }

    public static void getp(int num, Vector2f[] in) {
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

    public static void getp(int width, int height, int num, Vector2f[] in) {
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
