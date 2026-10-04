package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.exceptions.FailedRecipeLoadException;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipesUtil {

  public static void loadRecipeFiles() {
    if(Bukkit.getPluginManager().getPlugin("Nexo") == null
        || !Bukkit.getPluginManager().getPlugin("Nexo").isEnabled()) return;

    File recipeFolder = new File(Bukkit.getPluginManager().getPlugin("Nexo").getDataFolder()+"/recipes/smithing");
    if (!recipeFolder.exists()) recipeFolder.mkdirs();

    RecipeManager.setExampleFile(new File(recipeFolder, "smithing_example.yml"));
    RecipeManager.setRecipesFiles(RecipesUtil.obtainRecipesFiles(recipeFolder));

    if (!RecipeManager.getExampleFile().exists()) {
      try {
        InputStream resourceStream = NexoAddon.getInstance().getResource("recipes/smithing_example.yml");
        if (resourceStream == null) {
          return;
        }

        Files.copy(resourceStream, RecipeManager.getExampleFile().toPath());
      } catch (IOException e) {
        NexoAddon.getInstance().getLogger().severe("Failed to generate smithing_example.yml: " + e.getMessage());
      }
    }
  }

  private static List<File> obtainRecipesFiles(File folder) {
    try (Stream<Path> stream  = Files.walk(folder.toPath())) {
      return stream.filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".yml"))
              .map(Path::toFile)
              .collect(Collectors.toList());
    } catch (IOException e) {
      NexoAddon.getInstance().getLogger().warning("Error during folder reading: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  public static void loadRecipes()
  {
    RecipesUtil.loadRecipeFiles();
    CustomCraftingUtil.loadStations();

    if(RecipeManager.getRecipesFiles().isEmpty()) return;

    RecipeManager.getRecipesFiles().forEach(file -> {

      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

      yaml.getKeys(false).forEach(recipeId -> {
        try {
          RecipeManager.addSmithingTransformRecipe(recipeId, yaml);
        } catch (FailedRecipeLoadException e) {
          NexoAddon.getInstance().getLogger().warning(e.getMessage());
        }
      });

    });
  }
}
