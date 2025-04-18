package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class RecipesUtil {

  public static void loadRecipeFile() {
    if(Bukkit.getPluginManager().getPlugin("Nexo") == null
        || !Bukkit.getPluginManager().getPlugin("Nexo").isEnabled()) return;

    File recipeFolder = new File(Bukkit.getPluginManager().getPlugin("Nexo").getDataFolder(), "recipes");
    if (!recipeFolder.exists()) {
      recipeFolder.mkdirs();
    }

    RecipeManager.setRecipeFile(new File(recipeFolder, "smithing.yml"));

    if (!RecipeManager.getRecipeFile().exists()) {
      try {
        InputStream resourceStream = NexoAddon.getInstance().getResource("recipes/smithing.yml");
        if (resourceStream == null) {
          return;
        }

        Files.copy(resourceStream, RecipeManager.getRecipeFile().toPath());
      } catch (IOException e) {
        NexoAddon.getInstance().getLogger().severe("Failed to generate smithing.yml: " + e.getMessage());
      }
    }

    RecipeManager.setRecipeConfig(YamlConfiguration.loadConfiguration(RecipeManager.getRecipeFile()));
  }

  public static void loadRecipes() {
    loadRecipeFile();
    if (RecipeManager.getRecipeConfig() == null) {
      NexoAddon.getInstance().getLogger().severe("Recipes file not found");
      return;
    }

    RecipeManager.getRecipeConfig().getKeys(false).forEach(recipeId -> {
      try {
        RecipeManager.addSmithingTransformRecipe(recipeId, RecipeManager.getRecipeConfig());
      } catch (IllegalArgumentException e) {
        NexoAddon.getInstance().getLogger().warning("Failed to load recipe " + recipeId + ": " + e.getMessage());
      }
    });
  }
}
