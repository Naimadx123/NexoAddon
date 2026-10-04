package zone.vao.nexoAddon.populators;

import lombok.Getter;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

@Getter
public class BiomePopulator extends BlockPopulator {

  private final String id;
  private final Biome biome;
  private final List<String> worldNames;
  private final List<Biome> biomes;
  private final int minY;
  private final int maxY;
  private final double chance;

  public BiomePopulator(String id, Biome biome, List<String> worldNames, List<Biome> biomes, int minY, int maxY, double chance) {
    this.id = id;
    this.biome = biome;
    this.worldNames = List.copyOf(worldNames);
    this.biomes = List.copyOf(biomes);
    this.minY = minY;
    this.maxY = maxY;
    this.chance = chance;
  }

  public boolean appliesToWorld(String worldName) {
    return worldNames.contains("all") || worldNames.contains(worldName);
  }

  @Override
  public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
    if (!appliesToWorld(worldInfo.getName()) || random.nextDouble() >= chance) return;

    int bottom = Math.max(minY, worldInfo.getMinHeight());
    int top = Math.min(maxY, worldInfo.getMaxHeight() - 1);
    if (bottom > top) return;

    int startX = chunkX << 4;
    int startZ = chunkZ << 4;
    int startY = (bottom >> 2) << 2;

    for (int x = startX; x < startX + 16; x += 4) {
      for (int z = startZ; z < startZ + 16; z += 4) {
        for (int y = startY; y <= top; y += 4) {
          if (!limitedRegion.isInRegion(x, y, z)) continue;
          if (!biomes.contains(limitedRegion.getBiome(x, y, z))) continue;

          limitedRegion.setBiome(x, y, z, biome);
        }
      }
    }
  }
}
