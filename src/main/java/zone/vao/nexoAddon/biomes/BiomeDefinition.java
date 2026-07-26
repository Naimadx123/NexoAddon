package zone.vao.nexoAddon.biomes;

import com.google.gson.JsonObject;

public record BiomeDefinition(
    String namespace,
    String path,
    String sourceFile,
    String title,
    boolean enabled,
    JsonObject json
) {

  public String id() {
    return namespace + ":" + path;
  }

  public String packPath() {
    return "data/" + namespace + "/worldgen/biome/" + path + ".json";
  }
}
