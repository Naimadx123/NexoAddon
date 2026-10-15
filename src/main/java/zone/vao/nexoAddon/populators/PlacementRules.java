package zone.vao.nexoAddon.populators;

import org.bukkit.Material;

import java.util.List;

public record PlacementRules(List<Material> replace, List<Material> on, List<Material> below, List<Material> side, boolean airOnly) {

  public PlacementRules {
    replace = List.copyOf(replace);
    on = List.copyOf(on);
    below = List.copyOf(below);
    side = List.copyOf(side);
  }

  public boolean isEmpty() {
    return replace.isEmpty() && on.isEmpty() && below.isEmpty() && side.isEmpty();
  }
}
