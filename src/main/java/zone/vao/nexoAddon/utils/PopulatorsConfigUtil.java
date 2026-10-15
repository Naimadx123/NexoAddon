package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.populators.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

public class PopulatorsConfigUtil {

  private final File populatorsDir;
  private final File blocksDir;
  private final File biomesDir;
  private final ClassLoader pluginClassLoader;
  private final Logger logger;

  public PopulatorsConfigUtil(File pluginDirectory, ClassLoader pluginClassLoader, Logger logger) {
    populatorsDir = new File(pluginDirectory, "populators");
    blocksDir = new File(populatorsDir, "blocks");
    biomesDir = new File(populatorsDir, "biomes");
    this.pluginClassLoader = pluginClassLoader;
    this.logger = logger;
    createPopulatorFiles();
  }

  private void createPopulatorFiles() {
    try {
      Files.createDirectories(blocksDir.toPath());
      Files.createDirectories(biomesDir.toPath());
      copyResourceIfAbsent("block_populator.yml");
      copyResourceIfAbsent("biome_populator.yml");
    } catch (IOException exception) {
      logError("Failed to create populator files: " + exception.getMessage());
    }
  }

  private void copyResourceIfAbsent(String name) throws IOException {
    File file = new File(populatorsDir, name);
    if (file.exists()) return;
    try (InputStream input = pluginClassLoader.getResourceAsStream(name)) {
      if (input != null) Files.copy(input, file.toPath());
    }
  }

  public List<FileConfiguration> loadPopulatorConfigs() {
    List<FileConfiguration> configs = new ArrayList<>(loadDirectory(populatorsDir, "biome_populator.yml"));
    configs.addAll(loadDirectory(blocksDir, null));
    return configs;
  }

  private List<FileConfiguration> loadDirectory(File directory, String excluded) {
    File[] files = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".yml") && !file.getName().equals(excluded));
    if (files == null) return List.of();
    Arrays.sort(files, Comparator.comparing(File::getName));
    return Arrays.stream(files).map(file -> (FileConfiguration) YamlConfiguration.loadConfiguration(file)).toList();
  }

  public List<PopulatorDefinition> loadBlockPopulators() {
    List<PopulatorDefinition> result = new ArrayList<>();
    for (FileConfiguration config : loadPopulatorConfigs()) {
      for (String id : config.getKeys(false)) {
        ConfigurationSection section = config.getConfigurationSection(id);
        if (section == null || !section.getBoolean("enabled", true)) continue;
        try {
          int minY = integer(section, "minY", 0);
          int maxY = integer(section, "maxY", 0);
          if (minY > maxY) throw new IllegalArgumentException("minY must not exceed maxY");
          double chance = probability(section, "chance", 0.1);
          double clusterChance = probability(section, "cluster_chance", 0.0);
          PopulatorRange iterations = PopulatorRange.parse(section.get("iterations", 50), true);
          PopulatorRange veinSize = PopulatorRange.parse(section.get("vein_size", 0), false);
          List<String> worlds = worlds(section);
          BiomeFilter biomes = biomes(section);
          PlacementRules placement = new PlacementRules(materials(section, "replace"), materials(section, "place_on"),
              materials(section, "place_below"), materials(section, "place_side"), section.getBoolean("air_only", false));
          if (placement.isEmpty()) throw new IllegalArgumentException("Specify replace, place_on, place_below or place_side");
          result.add(new PopulatorDefinition(item(id), minY, maxY, chance, iterations, veinSize, clusterChance, worlds, biomes, placement));
        } catch (IllegalArgumentException exception) {
          logError("Invalid populator `" + id + "`: " + exception.getMessage() + ". Skipping.");
        }
      }
    }
    return List.copyOf(result);
  }

  public List<BiomePopulator> loadBiomePopulatorsFromConfig() {
    List<FileConfiguration> configs = new ArrayList<>();
    File root = new File(populatorsDir, "biome_populator.yml");
    if (root.exists()) configs.add(YamlConfiguration.loadConfiguration(root));
    configs.addAll(loadDirectory(biomesDir, null));
    List<BiomePopulator> result = new ArrayList<>();
    for (FileConfiguration config : configs) {
      for (String id : config.getKeys(false)) {
        ConfigurationSection section = config.getConfigurationSection(id);
        if (section == null || !section.getBoolean("enabled", true)) continue;
        try {
          Biome biome = biome(section.getString("biome", ""));
          int minY = integer(section, "minY", Integer.MIN_VALUE);
          int maxY = integer(section, "maxY", Integer.MAX_VALUE);
          if (minY > maxY) throw new IllegalArgumentException("minY must not exceed maxY");
          result.add(new BiomePopulator(id, biome, worlds(section), biomes(section), minY, maxY, probability(section, "chance", 1.0)));
        } catch (IllegalArgumentException exception) {
          logError("Invalid biome populator `" + id + "`: " + exception.getMessage() + ". Skipping.");
        }
      }
    }
    return List.copyOf(result);
  }

  private PopulatorItem item(String id) {
    CustomBlockMechanic block = NexoBlocks.customBlockMechanic(id);
    if (block != null) {
      StringBlockMechanic string = NexoBlocks.stringMechanic(id);
      if (block.getBlockData() == null) throw new IllegalArgumentException("Custom block has no block data");
      return new PopulatorItem(id, block.getBlockData().clone(),
          string != null && string.getTall() ? Material.TRIPWIRE.createBlockData() : null, null,
          string != null && string.isSapling() ? string.sapling().getNaturalGrowthTime() : null);
    }
    FurnitureMechanic furniture = NexoFurniture.furnitureMechanic(id);
    if (furniture != null) return new PopulatorItem(id, null, null, furniture, null);
    Material material = Material.matchMaterial(id);
    if (material == null || !material.isBlock()) throw new IllegalArgumentException("Unknown block or furniture ID");
    return new PopulatorItem(id, material.createBlockData(), null, null, null);
  }

  private List<String> worlds(ConfigurationSection section) {
    List<String> names = strings(section, "worlds").stream().map(String::trim).filter(name -> !name.isEmpty()).distinct().toList();
    if (names.isEmpty()) throw new IllegalArgumentException("Specify at least one world");
    return names;
  }

  private BiomeFilter biomes(ConfigurationSection section) {
    Set<Biome> included = new HashSet<>();
    Set<Biome> excluded = new HashSet<>();
    for (String value : strings(section, "biomes")) {
      String name = value.trim();
      if (name.startsWith("!")) excluded.add(biome(name.substring(1)));
      else included.add(biome(name));
    }
    return new BiomeFilter(included, excluded);
  }

  private Biome biome(String name) {
    Biome biome = LiquidUtil.resolveBiome(name.trim().toLowerCase(Locale.ROOT));
    if (biome == null) throw new IllegalArgumentException("Unknown biome `" + name + "`");
    return biome;
  }

  private List<Material> materials(ConfigurationSection section, String key) {
    List<Material> result = new ArrayList<>();
    for (String name : strings(section, key)) {
      Material material = Material.matchMaterial(name.trim());
      if (material == null || !material.isBlock()) throw new IllegalArgumentException("Unknown block `" + name + "` in " + key);
      result.add(material);
    }
    return List.copyOf(result);
  }

  private int integer(ConfigurationSection section, String key, int defaultValue) {
    return Integer.parseInt(String.valueOf(section.get(key, defaultValue)));
  }

  private List<String> strings(ConfigurationSection section, String key) {
    Object value = section.get(key);
    if (value == null) return List.of();
    if (!(value instanceof List<?> list)) throw new IllegalArgumentException(key + " must be a list");
    List<String> result = new ArrayList<>();
    for (Object entry : list) {
      if (!(entry instanceof String text)) throw new IllegalArgumentException(key + " must contain strings");
      result.add(text);
    }
    return result;
  }

  private double probability(ConfigurationSection section, String key, double defaultValue) {
    double value = Double.parseDouble(String.valueOf(section.get(key, defaultValue)));
    if (!Double.isFinite(value) || value < 0.0 || value > 1.0) throw new IllegalArgumentException(key + " must be between 0.0 and 1.0");
    return value;
  }

  private void logError(String message) {
    logger.warning(message);
  }
}
