package zone.vao.nexoAddon.populators;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

public record LimitedPopulationRegion(LimitedRegion region) implements PopulationRegion {

  @Override
  public boolean contains(int x, int y, int z) {
    return region.isInRegion(x, y, z);
  }

  @Override
  public Material getType(int x, int y, int z) {
    return region.getType(x, y, z);
  }

  @Override
  public Biome getBiome(int x, int y, int z) {
    return region.getBiome(x, y, z);
  }

  @Override
  public void setBiome(int x, int y, int z, Biome biome) {
    region.setBiome(x, y, z, biome);
  }

  @Override
  public boolean place(PopulatorItem item, int x, int y, int z, BlockFace facing) {
    region.setBlockData(x, y, z, item.blockData(facing));
    if (item.upperData() != null) region.setBlockData(x, y + 1, z, item.upperData().clone());
    return true;
  }
}
