package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.populators.orePopulator.Ore;
import zone.vao.nexoAddon.populators.treePopulator.CustomTree;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PopulatorsConfigUtil {

  private final File populatorsDir;
  private final File blocksDir;
  private final File treesDir;
  private final ClassLoader pluginClassLoader;

  public PopulatorsConfigUtil(File pluginDirectory, ClassLoader pluginClassLoader) {
    this.populatorsDir = new File(pluginDirectory, "populators");
    this.blocksDir = new File(populatorsDir, "blocks");
    this.treesDir = new File(populatorsDir, "trees");
    this.pluginClassLoader = pluginClassLoader;
    createPopulatorFiles();
  }

  private void createPopulatorFiles() {
    if (!populatorsDir.exists() && !populatorsDir.mkdirs()) {
      NexoAddon.getInstance().getLogger().severe("Failed to create populators directory.");
      return;
    }

    if (!blocksDir.exists() && !blocksDir.mkdirs()) {
      NexoAddon.getInstance().getLogger().severe("Failed to create blocks directory.");
      return;
    }

    if (!treesDir.exists() && !treesDir.mkdirs()) {
      NexoAddon.getInstance().getLogger().severe("Failed to create trees directory.");
      return;
    }

    copyResourceIfAbsent("block_populator.yml", populatorsDir);
    copyResourceIfAbsent("tree_populator.yml", populatorsDir);
  }

  private void copyResourceIfAbsent(String fileName, File targetDir) {
    File file = new File(targetDir, fileName);
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
    List<FileConfiguration> configs = new ArrayList<>();

    configs.addAll(loadConfigsFromDirectory(populatorsDir));

    configs.addAll(loadConfigsFromDirectory(blocksDir));

    configs.addAll(loadConfigsFromDirectory(treesDir));

    return configs;
  }

  private List<FileConfiguration> loadConfigsFromDirectory(File directory) {
    if (!directory.exists()) {
      logError("Directory does not exist: " + directory.getName());
      return Collections.emptyList();
    }

    File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files == null) {
      logError("Failed to list files in the directory: " + directory.getName());
      return Collections.emptyList();
    }

    return Arrays.stream(files)
            .map(YamlConfiguration::loadConfiguration)
            .collect(Collectors.toList());
  }

  public List<Ore> loadOresFromConfig() {
    List<Ore> ores = new ArrayList<>();

    FileConfiguration rootConfig = loadConfigFile("block_populator.yml", populatorsDir);
    if (rootConfig != null) {
      ores.addAll(loadOresFromConfig(rootConfig));
    }

    for (FileConfiguration config : loadConfigsFromDirectory(blocksDir)) {
      ores.addAll(loadOresFromConfig(config));
    }

    return ores;
  }

  private List<Ore> loadOresFromConfig(FileConfiguration config) {
    return config.getKeys(false).stream()
            .map(key -> parseOre(key, config))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private Ore parseOre(String key, FileConfiguration config) {
    ConfigurationSection section = config.getConfigurationSection(key);

    Material material = Material.matchMaterial(key);

    if (section == null
            || (!NexoBlocks.isCustomBlock(key)
            && !NexoFurniture.isFurniture(key)
            && (material == null || !material.isBlock()))) {
      NexoAddon.getInstance().getLogger().warning("Incorrect key value! " + key);
      return null;
    }

    int minY = section.getInt("minY", 0);
    int maxY = Math.max(section.getInt("maxY", 0), minY);
    double chance = section.getDouble("chance", 0.1);
    Object iterations = parseIterationValue(section, "iterations", 50);
    Object veinSize = parseIterationValue(section, "vein_size", 0);
    double clusterChance = section.getDouble("cluster_chance", 0.0);

    List<String> worldNames = section.getStringList("worlds");
    NexoAddon.getInstance().getLogger().info(key + " worlds: " + worldNames);
    List<World> worlds = parseWorlds(worldNames);
    if (worlds.isEmpty()) return null;
    List<Biome> biomes = parseBiomes(worlds, section.getStringList("biomes"));

    List<Material> replaceMaterials = parseMaterials(section.getStringList("replace"));
    List<Material> placeOnMaterials = parseMaterials(section.getStringList("place_on"));
    List<Material> placeBelowMaterials = parseMaterials(section.getStringList("place_below"));
    boolean airOnly = section.getBoolean("air_only", false);

    try {
      CustomBlockMechanic block = NexoBlocks.customBlockMechanic(key);
      FurnitureMechanic furniture = NexoFurniture.furnitureMechanic(key);
      if (block != null) {
        boolean isTall = false;
        StringBlockMechanic stringMechanic = NexoBlocks.stringMechanic(block.getItemID());
        if (stringMechanic != null) {
          isTall = stringMechanic.isTall();
        }
        return new Ore(key, block, minY, maxY, chance, replaceMaterials, placeOnMaterials, placeBelowMaterials, worlds, worldNames, biomes, iterations, isTall, veinSize, clusterChance, airOnly);
      } else if (furniture != null) {
        return new Ore(key, furniture, minY, maxY, chance, replaceMaterials, placeOnMaterials, placeBelowMaterials, worlds, worldNames, biomes, iterations, false, veinSize, clusterChance, airOnly);
      } else {
        return new Ore(key, material, minY, maxY, chance, replaceMaterials, placeOnMaterials, placeBelowMaterials, worlds, worldNames, biomes, iterations, false, veinSize, clusterChance, airOnly);
      }
    } catch (IllegalArgumentException e) {
      logError("Invalid custom block ID: " + key);
      return null;
    }
  }

  public List<CustomTree> loadTreesFromConfig() {
    List<CustomTree> trees = new ArrayList<>();

    FileConfiguration rootConfig = loadConfigFile("tree_populator.yml", populatorsDir);
    if (rootConfig != null) {
      trees.addAll(loadTreesFromConfig(rootConfig));
    }

    for (FileConfiguration config : loadConfigsFromDirectory(treesDir)) {
      trees.addAll(loadTreesFromConfig(config));
    }

    return trees;
  }

  private List<CustomTree> loadTreesFromConfig(FileConfiguration config) {
    return config.getKeys(false).stream()
            .map(key -> parseTree(key, config))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private CustomTree parseTree(String key, FileConfiguration config) {
    ConfigurationSection section = config.getConfigurationSection(key);

    if (section == null || !section.contains("logs") || !section.contains("leaves")) return null;

    String logs = section.getString("logs");
    String leaves = section.getString("leaves");

    if (!NexoBlocks.isCustomBlock(logs) || !NexoBlocks.isCustomBlock(leaves)) return null;

    int minY = section.getInt("minY", 0);
    int maxY = Math.max(section.getInt("maxY", 0), minY);
    double chance = section.getDouble("chance", 0.1);

    List<World> worlds = parseWorlds(section.getStringList("worlds"));
    List<Biome> biomes = parseBiomes(worlds, section.getStringList("biomes"));
    if (worlds.isEmpty()) return null;
    if (biomes.isEmpty())
      biomes = worlds.get(0).getBiomeProvider().getBiomes(worlds.get(0)).stream().toList();

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

  private FileConfiguration loadConfigFile(String fileName, File directory) {
    File file = new File(directory, fileName);
    if (!file.exists()) return null;
    return YamlConfiguration.loadConfiguration(file);
  }

  private List<World> parseWorlds(List<String> worldNames) {
    return worldNames.stream()
            .map(name -> {
              World world = NexoAddon.getInstance().getServer().getWorld(name);
              NexoAddon.getInstance().getLogger().warning(name + " found: " + (world != null));
              if (world == null) {
                NexoAddon.getInstance().getLogger().info("Loading world:" + name);
                world = loadWorld(name);
              }
              return world;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private World loadWorld(String worldName) {
    File worldFolder = new File(NexoAddon.getInstance().getServer().getWorldContainer(), worldName);
    if (!worldFolder.exists() || !worldFolder.isDirectory()) {
      NexoAddon.getInstance().getLogger().warning("World folder for " + worldName + " does not exist.");
      return null;
    }

    NexoAddon.getInstance().getLogger().info("Loaded world:" + worldName);
    WorldCreator creator = new WorldCreator(worldName);
    return NexoAddon.getInstance().getServer().createWorld(creator);
  }

  private List<Biome> parseBiomes(List<World> worlds, List<String> biomeNames) {
    List<Biome> availableBiomes = new ArrayList<>();

    Registry<Biome> biomeRegistry = Registry.BIOME;

    for (String biomeName : biomeNames) {

      NamespacedKey biomeKey;
      if (biomeName.contains(":")) {
        String[] parts = biomeName.split(":");
        biomeKey = new NamespacedKey(parts[0], parts[1]);
      } else {
        biomeKey = NamespacedKey.minecraft(biomeName.toLowerCase());
      }

      Biome biome = biomeRegistry.get(biomeKey);
      if (biome != null) {
        availableBiomes.add(biome);
      } else {
        NexoAddon.getInstance().getLogger().warning("Biome not found: " + biomeName);
      }
    }

    return availableBiomes.isEmpty() ? Registry.BIOME.stream().toList() : availableBiomes;
  }

  private List<Biome> getAllBiomes() {
    return Registry.BIOME.stream().toList();
  }

  private List<Material> parseMaterials(List<String> materialNames) {
    return materialNames.stream()
            .map(Material::getMaterial)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private Object parseIterationValue(ConfigurationSection section, String key, int defaultValue) {
    Object value = section.get(key, defaultValue);
    if (value instanceof String str) {
      if (str.contains("-")) {
        String[] parts = str.split("-");
        try {
          int min = Integer.parseInt(parts[0].trim());
          int max = Integer.parseInt(parts[1].trim());
          if (min > max) {
            int temp = min;
            min = max;
            max = temp;
          }
          return min + "-" + max;
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      } else {
        try {
          return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
    } else if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return defaultValue;
  }

  private void logError(String message) {
    NexoAddon.getInstance().getLogger().severe(message);
  }
}