package zone.vao.nexoAddon.biomes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public final class YamlJson {

  private YamlJson() {
  }

  public static JsonElement toJson(Object value) {
    if (value == null) return null;

    if (value instanceof ConfigurationSection section) {
      JsonObject object = new JsonObject();
      for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
        JsonElement converted = toJson(entry.getValue());
        if (converted != null) object.add(entry.getKey(), converted);
      }
      return object;
    }

    if (value instanceof Map<?, ?> map) {
      JsonObject object = new JsonObject();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        JsonElement converted = toJson(entry.getValue());
        if (converted != null) object.add(String.valueOf(entry.getKey()), converted);
      }
      return object;
    }

    if (value instanceof Collection<?> collection) {
      JsonArray array = new JsonArray();
      for (Object element : collection) {
        JsonElement converted = toJson(element);
        if (converted != null) array.add(converted);
      }
      return array;
    }

    if (value instanceof Number number) return new JsonPrimitive(number);
    if (value instanceof Boolean bool) return new JsonPrimitive(bool);
    if (value instanceof String string) return new JsonPrimitive(string);

    return new JsonPrimitive(String.valueOf(value));
  }

  public static JsonObject merge(JsonObject base, JsonObject overlay) {
    JsonObject result = base.deepCopy();
    if (overlay == null) return result;

    for (Map.Entry<String, JsonElement> entry : overlay.entrySet()) {
      String key = entry.getKey();
      JsonElement value = entry.getValue();

      if (value == null || value.isJsonNull()) continue;

      JsonElement existing = result.get(key);
      boolean clearing = value.isJsonObject() && value.getAsJsonObject().isEmpty();

      if (!clearing && existing != null && existing.isJsonObject() && value.isJsonObject()) {
        result.add(key, merge(existing.getAsJsonObject(), value.getAsJsonObject()));
      } else {
        result.add(key, value.deepCopy());
      }
    }

    return result;
  }

  public static void put(JsonObject root, JsonElement value, String... path) {
    JsonObject target = root;
    for (int i = 0; i < path.length - 1; i++) {
      JsonElement child = target.get(path[i]);
      if (child == null || !child.isJsonObject()) {
        JsonObject created = new JsonObject();
        target.add(path[i], created);
        target = created;
      } else {
        target = child.getAsJsonObject();
      }
    }
    target.add(path[path.length - 1], value);
  }

  public static int parseColor(Object raw) {
    if (raw instanceof Number number) {
      int value = number.intValue();
      return (value < 0 || value > 0xFFFFFF) ? -1 : value;
    }
    if (!(raw instanceof String text)) return -1;

    String cleaned = text.trim().toLowerCase(Locale.ROOT);
    if (cleaned.isEmpty()) return -1;

    int radix = 16;
    if (cleaned.startsWith("#")) {
      cleaned = cleaned.substring(1);
    } else if (cleaned.startsWith("0x")) {
      cleaned = cleaned.substring(2);
    } else if (cleaned.matches("\\d+")) {
      radix = 10;
    }

    try {
      int value = Integer.parseInt(cleaned, radix);
      return (value < 0 || value > 0xFFFFFF) ? -1 : value;
    } catch (NumberFormatException exception) {
      return -1;
    }
  }

  public static JsonElement colorValue(int rgb, boolean asHexString) {
    return asHexString ? new JsonPrimitive(String.format("#%06x", rgb)) : new JsonPrimitive(rgb);
  }
}
