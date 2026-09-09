package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.CustomCrafting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCraftingUtil {

  private static final Map<String, List<CustomCrafting.Recipe>> stations = new HashMap<>();

  public static void loadStations() {
    stations.clear();

    if (Bukkit.getPluginManager().getPlugin("Nexo") == null
        || !Bukkit.getPluginManager().getPlugin("Nexo").isEnabled()) return;

    File stationsFolder = new File(Bukkit.getPluginManager().getPlugin("Nexo").getDataFolder() + "/recipes/custom_crafting");
    if (!stationsFolder.exists()) stationsFolder.mkdirs();

    generateExample(stationsFolder);

    File[] folders = stationsFolder.listFiles(File::isDirectory);
    if (folders == null) return;

    for (File folder : folders) {
      File[] files = folder.listFiles(file -> file.isFile() && file.getName().endsWith(".yml"));
      if (files == null) continue;

      List<CustomCrafting.Recipe> recipes = new ArrayList<>();
      for (File file : files) {
        CustomCrafting.Recipe recipe = loadRecipe(file, folder.getName());
        if (recipe != null) recipes.add(recipe);
      }

      if (!recipes.isEmpty()) stations.put(folder.getName(), List.copyOf(recipes));
    }
  }

  public static List<CustomCrafting.Recipe> getRecipes(String station) {
    return stations.getOrDefault(station, List.of());
  }

  public static ItemStack buildItem(ConfigurationSection section) {
    if (section == null) return null;

    int amount = Math.max(1, section.getInt("amount", 1));
    String nexoId = section.getString("nexo_item");
    ItemBuilder itemBuilder = nexoId == null ? null : NexoItems.itemFromId(nexoId);

    if (itemBuilder != null) {
      ItemStack item = itemBuilder.build().clone();
      item.setAmount(amount);
      return item;
    }

    Material material = matchMaterial(section.getString("minecraft_item"));
    return material == null ? null : new ItemStack(material, amount);
  }

  private static CustomCrafting.Recipe loadRecipe(File file, String station) {
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

    ConfigurationSection ingredientsSection = yaml.getConfigurationSection("ingredients");
    ItemStack result = buildItem(yaml.getConfigurationSection("result"));

    if (ingredientsSection == null || result == null) {
      if (!yaml.getKeys(false).isEmpty())
        NexoAddon.getInstance().getLogger().warning("Custom crafting recipe `" + file.getName() + "` in station `"
            + station + "` has no valid `ingredients` or `result`. Skipping.");
      return null;
    }

    Map<Integer, CustomCrafting.Ingredient> ingredients = new HashMap<>();
    for (String rawSlot : ingredientsSection.getKeys(false)) {
      int slot = parseSlot(rawSlot, file.getName());
      if (slot < 0 || slot > 53) continue;

      CustomCrafting.Ingredient ingredient = buildIngredient(ingredientsSection.getConfigurationSection(rawSlot));
      if (ingredient == null) {
        NexoAddon.getInstance().getLogger().warning("Custom crafting ingredient in slot `" + rawSlot + "` of `"
            + file.getName() + "` is invalid. Skipping.");
        continue;
      }
      ingredients.put(slot, ingredient);
    }

    if (ingredients.isEmpty()) {
      NexoAddon.getInstance().getLogger().warning("Custom crafting recipe `" + file.getName() + "` in station `"
          + station + "` has no valid ingredients. Skipping.");
      return null;
    }

    return new CustomCrafting.Recipe(Map.copyOf(ingredients), result);
  }

  private static CustomCrafting.Ingredient buildIngredient(ConfigurationSection section) {
    if (section == null) return null;

    int amount = Math.max(1, section.getInt("amount", 1));
    String nexoId = section.getString("nexo_item");
    if (nexoId != null && NexoItems.itemFromId(nexoId) != null)
      return new CustomCrafting.Ingredient(nexoId, null, amount);

    Material material = matchMaterial(section.getString("minecraft_item"));
    return material == null ? null : new CustomCrafting.Ingredient(null, material, amount);
  }

  private static Material matchMaterial(String rawMaterial) {
    if (rawMaterial == null || rawMaterial.isBlank()) return null;

    return Material.matchMaterial(rawMaterial.trim());
  }

  private static int parseSlot(String rawSlot, String fileName) {
    try {
      return Integer.parseInt(rawSlot.trim());
    } catch (NumberFormatException exception) {
      NexoAddon.getInstance().getLogger().warning("Custom crafting slot `" + rawSlot + "` in `" + fileName
          + "` is not a number.");
      return -1;
    }
  }

  private static void generateExample(File stationsFolder) {
    File exampleFolder = new File(stationsFolder, "example_station");
    if (exampleFolder.exists()) return;

    exampleFolder.mkdirs();

    try {
      InputStream resourceStream = NexoAddon.getInstance().getResource("recipes/custom_crafting_example.yml");
      if (resourceStream == null) return;

      Files.copy(resourceStream, new File(exampleFolder, "example_recipe.yml").toPath());
    } catch (IOException e) {
      NexoAddon.getInstance().getLogger().severe("Failed to generate example_recipe.yml: " + e.getMessage());
    }
  }
}
