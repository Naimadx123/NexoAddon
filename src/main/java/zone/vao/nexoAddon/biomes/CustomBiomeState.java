package zone.vao.nexoAddon.biomes;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CustomBiomeState {

  private static volatile Path dataDir;
  private static volatile Path packDir;
  private static volatile String startupHash;
  private static volatile String formatSummary;
  private static volatile List<BiomeDefinition> definitions = List.of();
  private static volatile List<String> warnings = List.of();
  private static volatile boolean bootstrapRan;
  private static volatile String packCompatibility;

  private CustomBiomeState() {
  }

  public static void record(Path dataDirectory, BiomeGenerator.Result result) {
    dataDir = dataDirectory;
    packDir = result.packDir();
    startupHash = result.hash();
    formatSummary = result.formatSummary();
    definitions = result.definitions();
    warnings = result.warnings();
    bootstrapRan = true;
  }

  public static void markBootstrapRan() {
    bootstrapRan = true;
  }

  public static void recordCompatibility(String compatibility) {
    packCompatibility = compatibility;
  }

  public static Path dataDir() {
    return dataDir;
  }

  public static Path packDir() {
    return packDir;
  }

  public static String startupHash() {
    return startupHash;
  }

  public static String formatSummary() {
    return formatSummary;
  }

  public static String packCompatibility() {
    return packCompatibility;
  }

  public static List<BiomeDefinition> definitions() {
    return definitions;
  }

  public static List<String> warnings() {
    return warnings;
  }

  public static boolean bootstrapRan() {
    return bootstrapRan;
  }

  public static boolean isDefined(String id) {
    if (id == null || id.isBlank()) return false;

    String needle = id.toLowerCase(Locale.ROOT);
    if (!needle.contains(":")) needle = "minecraft:" + needle;

    for (BiomeDefinition definition : definitions) {
      if (definition.id().equalsIgnoreCase(needle)) return true;
    }
    return false;
  }

  public static boolean isEnabled(String id) {
    if (id == null || id.isBlank()) return false;

    String needle = id.toLowerCase(Locale.ROOT);
    if (!needle.contains(":")) needle = "minecraft:" + needle;

    for (BiomeDefinition definition : definitions) {
      if (definition.id().equalsIgnoreCase(needle)) return definition.enabled();
    }
    return false;
  }
}
