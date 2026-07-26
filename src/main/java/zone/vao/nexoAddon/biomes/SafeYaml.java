package zone.vao.nexoAddon.biomes;

import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SafeYaml {

  private SafeYaml() {
  }

  public record Result(YamlConfiguration config, String error) {

    public boolean failed() {
      return error != null;
    }
  }

  public static Result load(Path file, boolean literalKeys) {
    YamlConfiguration config = new YamlConfiguration();
    if (literalKeys) config.options().pathSeparator(' ');

    if (!Files.isRegularFile(file)) return new Result(config, null);

    try {
      config.loadFromString(Files.readString(file));
      return new Result(config, null);
    } catch (Throwable throwable) {
      String message = throwable.getMessage();
      return new Result(config, message == null ? throwable.toString() : message);
    }
  }

  public static YamlConfiguration loadQuietly(Path file) {
    return load(file, false).config();
  }
}
