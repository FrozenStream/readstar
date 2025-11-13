package frozenstream.readstar.data.star;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import frozenstream.readstar.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Optional;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class StarResourceReader extends SimplePreparableReloadListener<ArrayList<Star>> {
    private static final String starsJson = "custom/stars/stars.json";
    private static final ArrayList<Star> Stars = new ArrayList<>();
    @Override
    protected @NotNull ArrayList<Star> prepare(@NotNull ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        // 在后台线程执行
        profilerFiller.push("star prepare");
        ArrayList<Star> stars = new ArrayList<>();
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, starsJson);
        try {
            Optional<Resource> resourceOpt = resourceManager.getResource(loc);
            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                Constants.LOG.info("StarLoader: Reading stars json.");
                BufferedReader reader = resource.openAsReader();
                stars.addAll(readStarsJson(reader));
            } else Constants.LOG.info("StarLoader: read stars json failed.");
        } catch (Exception e) {
            Constants.LOG.error("Error reading custom.json: {}", e.getMessage());
        }
        profilerFiller.pop();
        return stars;
    }

    @Override
    protected void apply(@NotNull ArrayList<Star> stars, @NotNull ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        // 在主线程执行
        profilerFiller.push("star apply");
        Stars.clear();
        Stars.addAll(stars);
        Constants.LOG.info("StarLoader: {}", Stars.size());
        StarManager.init();
        profilerFiller.pop();
    }

    public static ImmutableList<Star> getStars() {
        return ImmutableList.copyOf(Stars);
    }


    private static ArrayList<Star> readStarsJson(Reader reader) {
        ArrayList<Star> stars = new ArrayList<>();
        JsonObject jsonObject;
        // 解析JSON
        jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray starsArray = jsonObject.getAsJsonArray("Stars");
        for (JsonElement element : starsArray) {
            JsonObject star = element.getAsJsonObject();
            String name = star.get("name").getAsString();
            JsonArray positionArray = star.getAsJsonArray("position");
            Vector3f position = new Vector3f(
                    positionArray.get(0).getAsFloat(),
                    positionArray.get(1).getAsFloat(),
                    positionArray.get(2).getAsFloat()
            );
            int type = star.get("type").getAsInt();
            float Vmag = star.get("Vmag").getAsFloat();

            stars.add(new Star(name, position.normalize(), type, Vmag));
        }
        return stars;
    }

    @SubscribeEvent
    static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new StarResourceReader());
    }
}
