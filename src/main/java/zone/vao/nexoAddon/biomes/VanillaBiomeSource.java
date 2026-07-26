package zone.vao.nexoAddon.biomes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class VanillaBiomeSource {

  public static final String DEFAULT_BASE = "minecraft:the_void";

  private static final String[] PROBE_CANDIDATES = {"minecraft:river", "minecraft:plains", DEFAULT_BASE};

  private final Map<String, JsonObject> cache = new HashMap<>();

  public record Schema(boolean hexColors, boolean colorsInAttributes) {

    static Schema fallback() {
      return new Schema(true, true);
    }
  }

  public JsonObject read(String id) {
    if (id == null || id.isBlank()) return null;

    String key = id.toLowerCase(Locale.ROOT);
    if (cache.containsKey(key)) {
      JsonObject cached = cache.get(key);
      return cached == null ? null : cached.deepCopy();
    }

    JsonObject loaded = load(key);
    cache.put(key, loaded);
    return loaded == null ? null : loaded.deepCopy();
  }

  private JsonObject load(String id) {
    String namespace = "minecraft";
    String path = id;
    int colon = id.indexOf(':');
    if (colon >= 0) {
      namespace = id.substring(0, colon);
      path = id.substring(colon + 1);
    }
    if (path.isBlank()) return null;

    String resource = "/data/" + namespace + "/worldgen/biome/" + path + ".json";
    try (InputStream stream = Bukkit.class.getResourceAsStream(resource)) {
      if (stream == null) return null;
      JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
    } catch (Throwable throwable) {
      return null;
    }
  }

  public Schema probe() {
    for (String candidate : PROBE_CANDIDATES) {
      JsonObject biome = read(candidate);
      if (biome == null) continue;

      JsonElement waterColor = biome.has("effects")
          && biome.get("effects").isJsonObject()
          ? biome.getAsJsonObject("effects").get("water_color")
          : null;
      if (waterColor == null || !waterColor.isJsonPrimitive()) continue;

      boolean hexColors = waterColor.getAsJsonPrimitive().isString();
      boolean colorsInAttributes = biome.has("attributes")
          && biome.get("attributes").isJsonObject()
          && biome.getAsJsonObject("attributes").has("minecraft:visual/sky_color");

      return new Schema(hexColors, colorsInAttributes);
    }
    return null;
  }

  public JsonObject defaultBase() {
    JsonObject base = read(DEFAULT_BASE);
    if (base == null) return null;

    if (base.has("features") && base.get("features").isJsonArray()) {
      com.google.gson.JsonArray steps = base.getAsJsonArray("features");
      com.google.gson.JsonArray blanked = new com.google.gson.JsonArray();
      for (int i = 0; i < steps.size(); i++) blanked.add(new com.google.gson.JsonArray());
      base.add("features", blanked);
    }
    return base;
  }
}
