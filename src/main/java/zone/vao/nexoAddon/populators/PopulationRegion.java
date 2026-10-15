package zone.vao.nexoAddon.populators;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;

public interface PopulationRegion {

  boolean contains(int x, int y, int z);

  Material getType(int x, int y, int z);

  Biome getBiome(int x, int y, int z);

  void setBiome(int x, int y, int z, Biome biome);

  boolean place(PopulatorItem item, int x, int y, int z, BlockFace facing);
}
