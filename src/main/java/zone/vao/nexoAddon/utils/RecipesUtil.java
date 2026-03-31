package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.recipes.RecipesManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.exceptions.FailedRecipeLoadException;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

        if (resourceStream == null) return;

        Files.copy(resourceStream, RecipeManager.getExampleFile().toPath());
      }
      catch (IOException e) {
        NexoAddon.getInstance().getLogger().severe("Failed to generate smithing_example.yml: " + e.getMessage());
      }
    }
  }

  private static List<File> obtainRecipesFiles(File folder)
  {
    List<File> files = new ArrayList<>();
    if(!folder.isDirectory()) return files;
    if(folder.listFiles() == null) return files;

    for(File file : folder.listFiles())
    {
      if(file.isDirectory())
      {
        List<File> directoryFile = obtainRecipesFiles(file);
        if(!directoryFile.isEmpty()) files.addAll(directoryFile);
        continue;
      }
      if(file.getName().endsWith(".yml"))
      {
        files.add(file);
        continue;
      }
      Bukkit.getLogger().warning("Error during loading recipe file: " + file.getName() + ". Invalid recipes file");
    }
    return files;
  }

  public static void loadRecipes()
  {
    RecipesUtil.loadRecipeFiles();

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
