package zone.vao.nexoAddon.biomes;

import com.google.gson.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class BiomeGenerator {

  public static final String PACK_DIR_NAME = "generated_datapack";
  private static final String BIOMES_DIR_NAME = "custom_biomes";

  private static final Pattern NAMESPACE = Pattern.compile("^[a-z0-9._-]+$");
  private static final Pattern PATH = Pattern.compile("^[a-z0-9._-][a-z0-9._/-]*$");

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping()
      .create();

  private static final Set<String> EFFECT_COLORS =
      Set.of("water_color", "foliage_color", "grass_color", "dry_foliage_color");

  private static final Map<String, String> ATTRIBUTE_COLORS = Map.of(
      "sky_color", "minecraft:visual/sky_color",
      "fog_color", "minecraft:visual/fog_color",
      "water_fog_color", "minecraft:visual/water_fog_color"
  );

  private static final Set<String> KNOWN_KEYS = Set.of(
      "enabled", "inherit", "title", "raw",
      "water_color", "water_fog_color", "sky_color", "fog_color",
      "grass_color", "foliage_color", "dry_foliage_color", "grass_color_modifier",
      "temperature", "downfall", "has_precipitation", "temperature_modifier",
      "creature_spawn_probability"
  );

  private static final Set<String> BOOLEAN_KEYS = Set.of("has_precipitation");
  private static final Set<String> NUMBER_KEYS =
      Set.of("temperature", "downfall", "creature_spawn_probability");

  private static final Set<String> SCALAR_KEYS = Set.of(
      "enabled", "inherit", "title",
      "water_color", "water_fog_color", "sky_color", "fog_color",
      "grass_color", "foliage_color", "dry_foliage_color", "grass_color_modifier",
      "temperature", "downfall", "has_precipitation", "temperature_modifier",
      "creature_spawn_probability"
  );

  private static final Pattern COMMENTED_VALUE =
      Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*:\\s*#.*$");

  public record Result(
      Path packDir,
      List<BiomeDefinition> definitions,
      List<String> warnings,
      boolean aborted,
      int written,
      String hash,
      String formatSummary
  ) {
  }

  private final Path dataDir;
  private final String defaultNamespace;
  private final String defaultInherit;
  private final boolean pruneOrphans;

  private final List<String> warnings = new ArrayList<>();
  private final VanillaBiomeSource vanilla = new VanillaBiomeSource();

  public BiomeGenerator(Path dataDir, String defaultNamespace, String defaultInherit, boolean pruneOrphans) {
    this.dataDir = dataDir;
    this.defaultNamespace = defaultNamespace == null || defaultNamespace.isBlank() ? "nexoaddon" : defaultNamespace;
    this.defaultInherit = defaultInherit == null || defaultInherit.isBlank()
        ? VanillaBiomeSource.DEFAULT_BASE : defaultInherit;
    this.pruneOrphans = pruneOrphans;
  }

  public Result run() {
    Path packDir = dataDir.resolve(PACK_DIR_NAME);
    PackMeta.Format format = PackMeta.detect();

    try {
      return generate(packDir, format);
    } catch (Throwable throwable) {
      warnings.add("Biome generation failed unexpectedly: " + throwable);
      return new Result(packDir, List.of(), List.copyOf(warnings), true, 0, null, format.summary());
    }
  }

  private Result generate(Path packDir, PackMeta.Format format) throws IOException {
    Path biomesDir = dataDir.resolve(BIOMES_DIR_NAME);
    Files.createDirectories(biomesDir);
    seedExample(biomesDir);

    VanillaBiomeSource.Schema schema = vanilla.probe();
    if (schema == null) {
      schema = VanillaBiomeSource.Schema.fallback();
      warnings.add("Could not read any vanilla biome from the server jar to detect the colour schema; "
          + "assuming the MC 26.1 layout. Use `raw:` if colours land in the wrong place.");
    }

    List<BiomeDefinition> definitions = new ArrayList<>();
    Map<String, String> claimedBy = new HashMap<>();
    boolean aborted = false;

    for (Path file : sortedYamlFiles(biomesDir)) {
      String name = biomesDir.relativize(file).toString().replace('\\', '/');

      SafeYaml.Result loaded = SafeYaml.load(file, true);
      if (loaded.failed()) {
        warnings.add("custom_biomes/" + name + ": invalid YAML (" + loaded.error()
            + "). Skipping the whole file; no biomes were written.");
        aborted = true;
        continue;
      }

      if (hasCommentedValues(name, file)) {
        aborted = true;
        continue;
      }

      YamlConfiguration config = loaded.config();
      for (String key : config.getKeys(false)) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section == null) {
          warnings.add("custom_biomes/" + name + ": `" + key + "` is not a section. Skipping.");
          aborted = true;
          continue;
        }

        BiomeDefinition definition = build(name, key, section, schema);
        if (definition == null) {
          aborted = true;
          continue;
        }

        String previous = claimedBy.putIfAbsent(definition.id(), name);
        if (previous != null) {
          warnings.add("Biome `" + definition.id() + "` is defined twice (custom_biomes/" + previous
              + " and custom_biomes/" + name + "). Keeping the first.");
          continue;
        }
        definitions.add(definition);
      }
    }

    if (aborted) {
      warnings.add("Because at least one biome definition was invalid, the datapack was NOT rewritten. "
          + "Existing generated biomes are untouched. Fix the errors above and restart.");
      return new Result(packDir, List.copyOf(definitions), List.copyOf(warnings), true, 0, null, format.summary());
    }

    List<BiomeDefinition> active = definitions.stream().filter(BiomeDefinition::enabled).toList();

    Map<String, byte[]> files = new LinkedHashMap<>();
    files.put("pack.mcmeta",
        (GSON.toJson(PackMeta.build(format, "NexoAddon generated biomes")) + "\n").getBytes(StandardCharsets.UTF_8));
    for (BiomeDefinition definition : active) {
      files.put(definition.packPath(), (GSON.toJson(definition.json()) + "\n").getBytes(StandardCharsets.UTF_8));
    }

    int written = writeIfChanged(packDir, files);
    handleOrphans(packDir, files.keySet());

    return new Result(packDir, List.copyOf(definitions), List.copyOf(warnings), false, written,
        hash(files), format.summary());
  }

  private boolean hasCommentedValues(String name, Path file) {
    List<String> lines;
    try {
      lines = Files.readAllLines(file);
    } catch (Throwable throwable) {
      return false;
    }

    boolean found = false;
    for (int i = 0; i < lines.size(); i++) {
      var matcher = COMMENTED_VALUE.matcher(lines.get(i));
      if (!matcher.matches()) continue;

      String key = matcher.group(1);
      if (!SCALAR_KEYS.contains(key)) continue;

      warnings.add("custom_biomes/" + name + " line " + (i + 1) + ": `" + key
          + "` has no value because YAML read the rest of the line as a comment. If you meant a colour,"
          + " quote it: " + key + ": \"#8b0000\". Skipping the whole file; no biomes were written.");
      found = true;
    }
    return found;
  }

  private void seedExample(Path biomesDir) {
    Path target = biomesDir.resolve("example_biomes.yml");
    try {
      if (Files.exists(target)) return;
      try (java.io.InputStream stream =
               BiomeGenerator.class.getResourceAsStream("/custom_biomes/example_biomes.yml")) {
        if (stream != null) Files.copy(stream, target);
      }
    } catch (Throwable throwable) {
      warnings.add("Could not write the example biome file: " + throwable);
    }
  }

  private List<Path> sortedYamlFiles(Path dir) throws IOException {
    try (Stream<Path> stream = Files.walk(dir)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".yml"))
          .sorted(Comparator.comparing(path -> dir.relativize(path).toString()))
          .toList();
    }
  }

  private BiomeDefinition build(String file, String key, ConfigurationSection section,
                                VanillaBiomeSource.Schema schema) {
    String where = "custom_biomes/" + file + " -> " + key;

    String namespace = defaultNamespace;
    String path = key.toLowerCase(Locale.ROOT);
    int colon = path.indexOf(':');
    if (colon >= 0) {
      namespace = path.substring(0, colon);
      path = path.substring(colon + 1);
    }

    if (!NAMESPACE.matcher(namespace).matches()) {
      warnings.add(where + ": invalid namespace `" + namespace + "` (allowed: a-z 0-9 . _ -). Skipping.");
      return null;
    }
    if (path.isEmpty() || !PATH.matcher(path).matches() || path.endsWith("/")) {
      warnings.add(where + ": invalid biome id `" + path + "` (allowed: a-z 0-9 . _ - /). Skipping.");
      return null;
    }
    if (namespace.equals("minecraft")) {
      warnings.add(where + ": refusing to overwrite the `minecraft` namespace. Use your own. Skipping.");
      return null;
    }

    String inherit = section.getString("inherit", defaultInherit);
    JsonObject base;
    if (VanillaBiomeSource.DEFAULT_BASE.equalsIgnoreCase(inherit)) {
      base = vanilla.defaultBase();
    } else {
      base = vanilla.read(inherit);
    }
    if (base == null) {
      warnings.add(where + ": could not read base biome `" + inherit
          + "` from the server jar. Skipping (define every field via `raw:` if this persists).");
      return null;
    }

    for (String candidate : section.getKeys(false)) {
      if (!KNOWN_KEYS.contains(candidate)) {
        warnings.add(where + ": unknown key `" + candidate + "`; use `raw:` for arbitrary biome JSON.");
      }
    }

    for (String colorKey : EFFECT_COLORS) {
      if (!section.contains(colorKey)) continue;
      Integer rgb = color(section, colorKey, where);
      if (rgb == null) return null;
      YamlJson.put(base, YamlJson.colorValue(rgb, schema.hexColors()), "effects", colorKey);
    }
    for (Map.Entry<String, String> entry : ATTRIBUTE_COLORS.entrySet()) {
      if (!section.contains(entry.getKey())) continue;
      Integer rgb = color(section, entry.getKey(), where);
      if (rgb == null) return null;
      JsonElement value = YamlJson.colorValue(rgb, schema.hexColors());
      if (schema.colorsInAttributes()) {
        YamlJson.put(base, value, "attributes", entry.getValue());
      } else {
        YamlJson.put(base, value, "effects", entry.getKey());
      }
    }

    if (section.contains("grass_color_modifier")) {
      String modifier = String.valueOf(section.get("grass_color_modifier")).toLowerCase(Locale.ROOT);
      if (!Set.of("none", "dark_forest", "swamp").contains(modifier)) {
        warnings.add(where + ": unusual grass_color_modifier `" + modifier + "`; passing it through.");
      }
      YamlJson.put(base, new JsonPrimitive(modifier), "effects", "grass_color_modifier");
    }

    for (String numberKey : NUMBER_KEYS) {
      if (!section.contains(numberKey)) continue;
      Object raw = section.get(numberKey);
      if (!(raw instanceof Number number)) {
        warnings.add(where + ": `" + numberKey + "` must be a number but was `" + raw + "`. Skipping biome.");
        return null;
      }
      if (numberKey.equals("downfall") && (number.doubleValue() < 0 || number.doubleValue() > 1)) {
        warnings.add(where + ": downfall " + number + " is outside 0.0-1.0; passing it through.");
      }
      base.add(numberKey, new JsonPrimitive(number));
    }

    for (String booleanKey : BOOLEAN_KEYS) {
      if (!section.contains(booleanKey)) continue;
      Object raw = section.get(booleanKey);
      if (!(raw instanceof Boolean bool)) {
        warnings.add(where + ": `" + booleanKey + "` must be true or false but was `" + raw
            + "`. Write it unquoted as `true` or `false`. Skipping biome.");
        return null;
      }
      base.add(booleanKey, new JsonPrimitive(bool));
    }

    if (section.contains("temperature_modifier")) {
      base.add("temperature_modifier",
          new JsonPrimitive(String.valueOf(section.get("temperature_modifier")).toLowerCase(Locale.ROOT)));
    }

    if (section.isConfigurationSection("raw")) {
      JsonElement raw = YamlJson.toJson(section.getConfigurationSection("raw"));
      if (raw != null && raw.isJsonObject()) {
        base = YamlJson.merge(base, raw.getAsJsonObject());
      }
    }

    return new BiomeDefinition(namespace, path, file,
        section.getString("title", path), section.getBoolean("enabled", false), base);
  }

  private Integer color(ConfigurationSection section, String key, String where) {
    Object raw = section.get(key);
    if (raw == null) {
      warnings.add(where + ": `" + key + "` has no value. This usually means an unquoted `#` colour — "
          + "YAML reads `#8b0000` as a comment, so write it as \"#8b0000\". Skipping biome.");
      return null;
    }
    int rgb = YamlJson.parseColor(raw);
    if (rgb < 0) {
      warnings.add(where + ": `" + key + "` is not a colour: `" + raw
          + "`. Use \"#rrggbb\". Skipping biome.");
      return null;
    }
    return rgb;
  }

  private int writeIfChanged(Path packDir, Map<String, byte[]> files) throws IOException {
    int changed = 0;
    for (Map.Entry<String, byte[]> entry : files.entrySet()) {
      Path target = packDir.resolve(entry.getKey());
      byte[] desired = entry.getValue();

      if (Files.isRegularFile(target) && Arrays.equals(Files.readAllBytes(target), desired)) continue;

      Files.createDirectories(target.getParent());
      Files.write(target, desired);
      changed++;
    }
    return changed;
  }

  private void handleOrphans(Path packDir, Set<String> expected) throws IOException {
    Path dataRoot = packDir.resolve("data");
    if (!Files.isDirectory(dataRoot)) return;

    Set<String> keep = new HashSet<>(expected);
    List<Path> orphans;
    try (Stream<Path> stream = Files.walk(dataRoot)) {
      orphans = stream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .filter(path -> !keep.contains(packDir.relativize(path).toString().replace('\\', '/')))
          .toList();
    }

    for (Path orphan : orphans) {
      String relative = packDir.relativize(orphan).toString().replace('\\', '/');
      if (pruneOrphans) {
        Files.deleteIfExists(orphan);
        warnings.add("Removed generated biome with no definition: " + relative
            + " (custom_biomes.prune_orphans is on). Chunks already painted with it will fail to load.");
      } else {
        warnings.add("Generated biome " + relative + " has no matching entry in custom_biomes/ anymore. "
            + "Keeping it so existing chunks still load; set custom_biomes.prune_orphans: true to delete it.");
      }
    }
  }

  private String hash(Map<String, byte[]> files) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      files.keySet().stream().sorted().forEach(key -> {
        digest.update(key.getBytes(StandardCharsets.UTF_8));
        digest.update(files.get(key));
      });
      StringBuilder builder = new StringBuilder();
      for (byte b : digest.digest()) builder.append(String.format("%02x", b));
      return builder.toString();
    } catch (Throwable throwable) {
      return null;
    }
  }
}
