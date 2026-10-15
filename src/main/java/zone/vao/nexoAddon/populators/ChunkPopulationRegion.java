package zone.vao.nexoAddon.populators;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator.ChunkData;

public class ChunkPopulationRegion implements PopulationRegion {

  private final ChunkData data;
  private final int chunkX;
  private final int chunkZ;
  private final int bottom;
  private final int top;
  private Biome[] biomes;

  public ChunkPopulationRegion(ChunkData data, int chunkX, int chunkZ) {
    this.data = data;
    this.chunkX = chunkX;
    this.chunkZ = chunkZ;
    bottom = data.getMinHeight();
    top = data.getMaxHeight();
  }

  @Override
  public boolean contains(int x, int y, int z) {
    return (x >> 4) == chunkX && (z >> 4) == chunkZ && y >= bottom && y < top;
  }

  @Override
  public Material getType(int x, int y, int z) {
    return data.getType(x & 15, y, z & 15);
  }

  @Override
  public Biome getBiome(int x, int y, int z) {
    if (biomes != null) {
      Biome biome = biomes[biomeIndex(x, y, z)];
      if (biome != null) return biome;
    }
    return data.getBiome(x & 15, y, z & 15);
  }

  @Override
  public void setBiome(int x, int y, int z, Biome biome) {
    if (biomes == null) biomes = new Biome[(((top - 1) >> 2) - (bottom >> 2) + 1) * 16];
    biomes[biomeIndex(x, y, z)] = biome;
  }

  @Override
  public boolean place(PopulatorItem item, int x, int y, int z, BlockFace facing) {
    data.setBlock(x & 15, y, z & 15, item.blockData(facing));
    if (item.upperData() != null) data.setBlock(x & 15, y + 1, z & 15, item.upperData().clone());
    return true;
  }

  private int biomeIndex(int x, int y, int z) {
    return ((y >> 2) - (bottom >> 2)) * 16 + ((z & 15) >> 2) * 4 + ((x & 15) >> 2);
  }
}
