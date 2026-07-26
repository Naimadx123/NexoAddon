package zone.vao.nexoAddon.biomes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class PackMeta {

  private static final int FALLBACK_MAJOR = 101;

  private PackMeta() {
  }

  public record Format(Integer major, Integer legacy, String origin) {

    public boolean isModern() {
      return major != null;
    }

    public String summary() {
      return isModern()
          ? "max_format=" + major + " min_format=[" + major + ",0] (from " + origin + ")"
          : "pack_format=" + legacy + " (from " + origin + ")";
    }
  }

  public static Format detect() {
    Format fromVersion = fromVersionJson();
    if (fromVersion != null) return fromVersion;

    Format fromVanilla = fromVanillaPack();
    if (fromVanilla != null) return fromVanilla;

    return new Format(FALLBACK_MAJOR, null, "compiled fallback");
  }

  private static Format fromVersionJson() {
    JsonObject root = readJson("/version.json");
    if (root == null || !root.has("pack_version") || !root.get("pack_version").isJsonObject()) return null;

    JsonObject packVersion = root.getAsJsonObject("pack_version");
    if (packVersion.has("data_major")) {
      try {
        return new Format(packVersion.get("data_major").getAsInt(), null, "version.json");
      } catch (Throwable ignored) {
      }
    }
    if (packVersion.has("data")) {
      try {
        return new Format(null, packVersion.get("data").getAsInt(), "version.json");
      } catch (Throwable ignored) {
      }
    }
    return null;
  }

  private static Format fromVanillaPack() {
    JsonObject root = readJson("/data/minecraft/datapacks/trade_rebalance/pack.mcmeta");
    if (root == null || !root.has("pack") || !root.get("pack").isJsonObject()) return null;

    JsonObject pack = root.getAsJsonObject("pack");
    try {
      if (pack.has("max_format")) {
        return new Format(pack.get("max_format").getAsInt(), null, "vanilla pack.mcmeta");
      }
      if (pack.has("pack_format")) {
        return new Format(null, pack.get("pack_format").getAsInt(), "vanilla pack.mcmeta");
      }
    } catch (Throwable ignored) {
    }
    return null;
  }

  private static JsonObject readJson(String resource) {
    try (InputStream stream = Bukkit.class.getResourceAsStream(resource)) {
      if (stream == null) return null;
      JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
    } catch (Throwable throwable) {
      return null;
    }
  }

  public static JsonObject build(Format format, String description) {
    JsonObject pack = new JsonObject();
    pack.addProperty("description", description);

    if (format.isModern()) {
      pack.addProperty("max_format", format.major());
      JsonArray min = new JsonArray();
      min.add(format.major());
      min.add(0);
      pack.add("min_format", min);
    } else {
      pack.addProperty("pack_format", format.legacy());
    }

    JsonObject root = new JsonObject();
    root.add("pack", pack);
    return root;
  }
}
