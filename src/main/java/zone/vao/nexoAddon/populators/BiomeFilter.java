package zone.vao.nexoAddon.populators;

import org.bukkit.block.Biome;

import java.util.Set;

public record BiomeFilter(Set<Biome> included, Set<Biome> excluded) {

  public BiomeFilter {
    included = Set.copyOf(included);
    excluded = Set.copyOf(excluded);
  }

  public boolean matches(Biome biome) {
    return (included.isEmpty() || included.contains(biome)) && !excluded.contains(biome);
  }
}
