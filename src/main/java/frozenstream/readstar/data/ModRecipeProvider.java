package frozenstream.readstar.data;

import frozenstream.readstar.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ASTRONOMICAL_MANUSCRIPT.get())
                .pattern("PPP")
                .pattern("B B")
                .pattern("PPP")
                .define('P', Items.PAPER)
                .define('B', Items.BOOK)
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_book", has(Items.BOOK))
                .save(recipeOutput, "astronomical_manuscript");
    }
}