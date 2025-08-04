package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2f;


public class Textures {
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

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
            return SUN_LOCATION;
        }
    }

    public static Vector2f[] getp(int num) {
        Vector2f[] ans = new Vector2f[4];
        int i = num % 4;
        int j = num / 4;
        float l = i / 4.0F;
        float t = j / 2.0F;
        float r = (i + 1) / 4.0F;
        float b = (j + 1) / 2.0F;
        ans[0] = new Vector2f(r, b);
        ans[1] = new Vector2f(l, b);
        ans[2] = new Vector2f(l, t);
        ans[3] = new Vector2f(r, t);
        return ans;
    }

    public static Vector2f[] getp(int width, int height, int num) {
        Vector2f[] ans = new Vector2f[4];
        int i = num % width;
        int j = num / width;
        float l = (float) i / width;
        float t = (float) j / height;
        float r = (float) (i + 1) / width;
        float b = (float) (j + 1) / height;
        ans[0] = new Vector2f(r, b);
        ans[1] = new Vector2f(l, b);
        ans[2] = new Vector2f(l, t);
        ans[3] = new Vector2f(r, t);
        return ans;
    }
}
