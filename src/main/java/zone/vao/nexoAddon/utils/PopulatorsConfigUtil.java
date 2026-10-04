package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.populators.BiomePopulator;
import zone.vao.nexoAddon.populators.orePopulator.Ore;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class PopulatorsConfigUtil {

  private final File populatorsDir;
  private final File blocksDir;
  private final File biomesDir;
  private final ClassLoader pluginClassLoader;

  public PopulatorsConfigUtil(File pluginDirectory, ClassLoader pluginClassLoader) {
    this.populatorsDir = new File(pluginDirectory, "populators");
    this.blocksDir = new File(populatorsDir, "blocks");
    this.biomesDir = new File(populatorsDir, "biomes");
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

    copyResourceIfAbsent("block_populator.yml", populatorsDir);
    if (!biomesDir.exists() && !biomesDir.mkdirs()) {
      NexoAddon.getInstance().getLogger().severe("Failed to create biomes directory.");
      return;
    }
    copyResourceIfAbsent("biome_populator.yml", populatorsDir);
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

    return configs;
  }

  private List<FileConfiguration> loadConfigsFromDirectory(File directory) {
    if (!directory.exists()) {
      logError("Directory does not exist: " + directory.getName());
      return Collections.emptyList();
    }

    File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml")
        && (!directory.equals(populatorsDir) || !name.equals("biome_populator.yml")));
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

  public List<BiomePopulator> loadBiomePopulatorsFromConfig() {
    List<FileConfiguration> configs = new ArrayList<>();
    FileConfiguration rootConfig = loadConfigFile("biome_populator.yml", populatorsDir);
    if (rootConfig != null) configs.add(rootConfig);
    configs.addAll(loadConfigsFromDirectory(biomesDir));

    List<BiomePopulator> populators = new ArrayList<>();
    for (FileConfiguration config : configs) {
      for (String id : config.getKeys(false)) {
        ConfigurationSection section = config.getConfigurationSection(id);
        if (section == null || !section.getBoolean("enabled", true)) continue;

        String biomeName = section.getString("biome");
        Biome biome = biomeName == null ? null : LiquidUtil.resolveBiome(biomeName.trim().toLowerCase(Locale.ROOT));
        if (biome == null) {
          logError("Unknown biome `" + biomeName + "` in biome populator `" + id
              + "`. Register custom biomes at startup and check /nexoaddon biomes. Skipping.");
          continue;
        }

        List<String> worldNames = section.getStringList("worlds");
        if (worldNames.isEmpty()) {
          logError("Biome populator `" + id + "` has no worlds. Skipping.");
          continue;
        }

        List<String> biomeNames = section.getStringList("biomes");
        List<Biome> biomes = parseBiomes(biomeNames);
        if (biomes.isEmpty()) {
          logError("Biome populator `" + id + "` matches no registered biomes. Skipping.");
          continue;
        }

        int minY = section.getInt("minY", Integer.MIN_VALUE);
        int maxY = section.getInt("maxY", Integer.MAX_VALUE);
        double chance = section.getDouble("chance", 1.0);
        if (minY > maxY || !Double.isFinite(chance) || chance < 0.0 || chance > 1.0) {
          logError("Invalid height range or chance in biome populator `" + id + "`. Skipping.");
          continue;
        }

        populators.add(new BiomePopulator(id, biome, worldNames, biomes, minY, maxY, chance));
      }
    }
    return List.copyOf(populators);
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
    List<World> worlds = worldNames.size() == 1 && worldNames.getFirst().equalsIgnoreCase("all")? Bukkit.getWorlds() : parseWorlds(worldNames);
    if (worlds.isEmpty()) return null;
    List<Biome> biomes = parseBiomes(section.getStringList("biomes"));
    if (biomes.isEmpty()) {
      logError("Populator `" + key + "` matches no registered biomes. Skipping.");
      return null;
    }

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
          isTall = stringMechanic.getTall();
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

  private FileConfiguration loadConfigFile(String fileName, File directory) {
    File file = new File(directory, fileName);
    if (!file.exists()) return null;
    return YamlConfiguration.loadConfiguration(file);
  }

  private List<World> parseWorlds(List<String> worldNames) {
    return worldNames.stream()
            .map(name -> NexoAddon.getInstance().getServer().getWorld(name))
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

  private List<Biome> parseBiomes(List<String> biomeNames) {
    if (Biome.class.isEnum()) {
      return parseBiomesOld(Arrays.asList(Biome.class.getEnumConstants()), biomeNames);
    } else {
      return parseBiomesNew(getAllBiomes(), biomeNames);
    }
  }

  private List<Biome> parseBiomesOld(List<Biome> biomes, List<String> biomeNames) {
    if (biomeNames.isEmpty()) return biomes;

    boolean isBlacklist = biomeNames.stream().anyMatch(name -> name.startsWith("!"));
    List<String> cleanNames = biomeNames.stream()
            .map(name -> name.replaceFirst("!", "").toUpperCase(Locale.ROOT))
            .toList();

    List<Biome> result = new ArrayList<>();
    try {
      Method nameMethod = Enum.class.getMethod("name");
      for (Biome biome : biomes) {
        String name = ((String) nameMethod.invoke(biome)).toUpperCase().replace(" ", "_");
        if (isBlacklist != cleanNames.contains(name)) result.add(biome);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }


  private List<Biome> parseBiomesNew(Collection<Biome> biomes, List<String> biomeNames) {
    if (biomeNames.isEmpty()) return new ArrayList<>(biomes);

    boolean isBlacklist = biomeNames.stream().anyMatch(name -> name.startsWith("!"));
    List<String> cleanNames = biomeNames.stream()
            .map(name -> name.replaceFirst("!", "").trim().toLowerCase(Locale.ROOT))
            .toList();

    List<Biome> result = new ArrayList<>();
    for (Biome biome : biomes) {
      NamespacedKey key = biome.getKey();
      boolean matches = cleanNames.contains(key.toString())
          || key.getNamespace().equals("minecraft") && cleanNames.contains(key.getKey());
      if (isBlacklist != matches) result.add(biome);
    }
    return result;
  }


  private List<Biome> getAllBiomes() {
    return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream().toList();
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
