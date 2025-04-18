package zone.vao.nexoAddon.utils.handlers;

import com.nexomc.nexo.api.NexoItems;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class RecipeManager {
    @Getter
    private static final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    @Getter
    @Setter
    private static File recipeFile;
    @Getter
    @Setter
    private static FileConfiguration recipeConfig;


    public static void addSmithingTransformRecipe(String recipeId, FileConfiguration config) {
        ItemStack resultTemplate = parseItem(config, recipeId + ".result");
        RecipeChoice.ExactChoice template = parseRecipeChoice(config, recipeId + ".template");
        RecipeChoice.ExactChoice base = parseRecipeChoice(config, recipeId + ".base");
        RecipeChoice.ExactChoice addition = parseRecipeChoice(config, recipeId + ".addition");

        if (resultTemplate == null || template == null || base == null || addition == null) {
            throw new IllegalArgumentException("Invalid recipe configuration for " + recipeId);
        }

        NamespacedKey key = new NamespacedKey(NexoAddon.getInstance(), recipeId);

        if (NexoAddon.getInstance().getServer().getRecipe(key) == null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    SmithingTransformRecipe recipe = new SmithingTransformRecipe(key, resultTemplate, template, base, addition);
                    NexoAddon.getInstance().getServer().addRecipe(recipe);
                    registeredRecipes.add(key);
                    NexoAddon.getInstance().getLogger().info("Registered smithing transform recipe: " + recipeId);
                }
            }.runTask(NexoAddon.getInstance());
        } else {
            NexoAddon.getInstance().getLogger().info("Recipe " + recipeId + " already exists, skipping.");
        }
    }

    private static ItemStack parseItem(FileConfiguration config, String path) {
        String nexoItemId = config.getString(path + ".nexo_item");
        if (nexoItemId != null && NexoItems.itemFromId(nexoItemId) != null)
            return NexoItems.itemFromId(nexoItemId).build().clone();

        String materialName = config.getString(path + ".minecraft_item");
        if(materialName == null) {
            NexoAddon.getInstance().getLogger().warning("Wrong item in " + path);
            return null;
        }
        Material material = Material.matchMaterial(materialName);
        return material != null ? new ItemStack(material).clone() : null;
    }

    private static RecipeChoice.ExactChoice parseRecipeChoice(FileConfiguration config, String path) {
        String nexoItemId = config.getString(path + ".nexo_item");
        if (nexoItemId != null && NexoItems.itemFromId(nexoItemId) != null)
            return new RecipeChoice.ExactChoice(NexoItems.itemFromId(nexoItemId).build().clone());

        String materialName = config.getString(path + ".minecraft_item");
        assert materialName != null;
        Material material = Material.matchMaterial(materialName);
        return material != null ? new RecipeChoice.ExactChoice(new ItemStack(material).clone()) : null;
    }

    public static void clearRegisteredRecipes() {
        if(registeredRecipes.isEmpty()) return;

        for (NamespacedKey key : registeredRecipes) {
            NexoAddon.getInstance().getServer().removeRecipe(key);
            NexoAddon.getInstance().getLogger().info("Removed recipe: " + key.getKey());
        }
        registeredRecipes.clear();
    }
}
