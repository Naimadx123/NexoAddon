package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.populators.orePopulator.Ore;
import zone.vao.nexoAddon.classes.populators.treePopulator.CustomTree;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PopulatorsConfigUtil {

  private final File populatorsDir;
  private final ClassLoader pluginClassLoader;

  public PopulatorsConfigUtil(File pluginDirectory, ClassLoader pluginClassLoader) {
    this.populatorsDir = new File(pluginDirectory, "populators");
    this.pluginClassLoader = pluginClassLoader;
    createPopulatorFiles();
  }

  private void createPopulatorFiles() {
    if (!populatorsDir.exists() && !populatorsDir.mkdirs()) {
      NexoAddon.getInstance().getLogger().severe("Failed to create populators directory.");
      return;
    }

    copyResourceIfAbsent("block_populator.yml");
    copyResourceIfAbsent("tree_populator.yml");
  }

  private void copyResourceIfAbsent(String fileName) {
    File file = new File(populatorsDir, fileName);
    if (file.exists()) return;

    try (InputStream inputStream = pluginClassLoader.getResourceAsStream(fileName);
         OutputStream outputStream = new FileOutputStream(file)) {
      if (inputStream == null) return;

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      NexoAddon.getInstance().getLogger().severe("Failed to copy " + fileName + ": " + e.getMessage());
    }
  }

  public List<FileConfiguration> loadPopulatorConfigs() {
    if (!populatorsDir.exists()) {
      logError("Populators directory does not exist.");
      return Collections.emptyList();
    }

    File[] files = populatorsDir.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files == null) {
      logError("Failed to list files in the populators directory.");
      return Collections.emptyList();
    }

    return Arrays.stream(files)
        .map(YamlConfiguration::loadConfiguration)
        .collect(Collectors.toList());
  }

  public List<Ore> loadOresFromConfig() {
    return loadConfigFile("block_populator.yml").getKeys(false).stream()
        .map(this::parseOre)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Ore parseOre(String key) {
    FileConfiguration config = loadConfigFile("block_populator.yml");
    ConfigurationSection section = config.getConfigurationSection(key);

    Material material = Material.matchMaterial(key);

    if (section == null
        || (!NexoBlocks.isCustomBlock(key)
        && !NexoFurniture.isFurniture(key)
        && (material == null || !material.isBlock()))) {
      return null;
    }

    int minY = section.getInt("minY", 0);
    int maxY = Math.max(section.getInt("maxY", 0), minY);
    double chance = section.getDouble("chance", 0.1);
    int iterations = Math.abs(section.getInt("iterations", 50));

    List<World> worlds = parseWorlds(section.getStringList("worlds"));
    List<Biome> biomes = parseBiomes(section.getStringList("biomes"));
    if(biomes.isEmpty())
      biomes = Arrays.stream(Biome.values()).toList();
    if (worlds.isEmpty()) return null;

    List<Material> replaceMaterials = parseMaterials(section.getStringList("replace"));
    List<Material> placeOnMaterials = parseMaterials(section.getStringList("place_on"));

    try {
      CustomBlockMechanic block = NexoBlocks.customBlockMechanic(key);
      FurnitureMechanic furniture = NexoFurniture.furnitureMechanic(key);
      if (block != null)
        return new Ore(block, minY, maxY, chance, replaceMaterials, placeOnMaterials, worlds, biomes, iterations);
      else if (furniture != null)
        return new Ore(furniture, minY, maxY, chance, replaceMaterials, placeOnMaterials, worlds, biomes, iterations);
      else
        return new Ore(material, minY, maxY, chance, replaceMaterials, placeOnMaterials, worlds, biomes, iterations);
    } catch (IllegalArgumentException e) {
      logError("Invalid custom block ID: " + key);
      return null;
    }
  }

  public List<CustomTree> loadTreesFromConfig() {
    return loadConfigFile("tree_populator.yml").getKeys(false).stream()
        .map(this::parseTree)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private CustomTree parseTree(String key) {
    FileConfiguration config = loadConfigFile("tree_populator.yml");
    ConfigurationSection section = config.getConfigurationSection(key);

    if (section == null || !section.contains("logs") || !section.contains("leaves")) return null;

    String logs = section.getString("logs");
    String leaves = section.getString("leaves");

    if (!NexoBlocks.isCustomBlock(logs) || !NexoBlocks.isCustomBlock(leaves)) return null;

    int minY = section.getInt("minY", 0);
    int maxY = Math.max(section.getInt("maxY", 0), minY);
    double chance = section.getDouble("chance", 0.1);

    List<World> worlds = parseWorlds(section.getStringList("worlds"));
    List<Biome> biomes = parseBiomes(section.getStringList("biomes"));
    if(biomes.isEmpty())
      biomes = Arrays.stream(Biome.values()).toList();

    if (worlds.isEmpty()) return null;

    try {
      return new CustomTree(
          key,
          NexoBlocks.customBlockMechanic(logs),
          NexoBlocks.customBlockMechanic(leaves),
          minY, maxY, chance, worlds, biomes);
    } catch (IllegalArgumentException e) {
      logError("Invalid custom block ID: " + key);
      return null;
    }
  }

  private FileConfiguration loadConfigFile(String fileName) {
    return YamlConfiguration.loadConfiguration(new File(populatorsDir, fileName));
  }

  private List<World> parseWorlds(List<String> worldNames) {
    return worldNames.stream()
        .map(Bukkit::getWorld)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private List<Biome> parseBiomes(List<String> biomeNames) {
    if (biomeNames.isEmpty()) return Arrays.asList(Biome.values());
    return biomeNames.stream()
        .map(b -> Biome.valueOf(b.trim().toUpperCase()))
        .collect(Collectors.toList());
  }

  private List<Material> parseMaterials(List<String> materialNames) {
    return materialNames.stream()
        .map(Material::getMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private void logError(String message) {
    NexoAddon.getInstance().getLogger().severe(message);
  }
}
