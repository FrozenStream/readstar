package frozenstream.readstar.data.planet;

import frozenstream.readstar.Constants;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;

//@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PlanetResourceReader extends SimplePreparableReloadListener<ArrayList<Planet>> {
    private static final String systemJson = "custom/planets/system.json";


    @Override
    protected ArrayList<Planet> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return null;
    }


    @Override
    protected void apply(ArrayList<Planet> planets, ResourceManager resourceManager, ProfilerFiller profilerFiller) {

    }
}
