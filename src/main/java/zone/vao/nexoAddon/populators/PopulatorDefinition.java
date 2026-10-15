package zone.vao.nexoAddon.populators;

import java.util.List;

public record PopulatorDefinition(PopulatorItem item, int minY, int maxY, double chance,
                                  PopulatorRange iterations, PopulatorRange veinSize, double clusterChance,
                                  List<String> worlds, BiomeFilter biomes, PlacementRules placement) {

  public PopulatorDefinition {
    worlds = List.copyOf(worlds);
  }

  public boolean appliesToWorld(String worldName) {
    return worlds.stream().anyMatch(name -> name.equalsIgnoreCase("all") || name.equals(worldName));
  }

  public boolean requiresDecoration() {
    return item.requiresWorld() || !placement.on().isEmpty() || !placement.below().isEmpty() || !placement.side().isEmpty();
  }
}
