package zone.vao.nexoAddon.classes.populators.treePopulator;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CustomTreePopulator extends BlockPopulator {

  private final TreePopulator treePopulator;

  public CustomTreePopulator(TreePopulator treePopulator) {
    this.treePopulator = treePopulator;
  }

  @Override
  public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
    for (CustomTree tree : treePopulator.getTrees()) {
      replaceTrees(worldInfo, random, chunkX, chunkZ, limitedRegion, tree);
    }
  }

  private void replaceTrees(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion, CustomTree tree) {
    if (random.nextDouble() > tree.getChance()) return;

    int startX = chunkX << 4;
    int startZ = chunkZ << 4;

    for (int x = startX; x < startX + 16; x++) {
      for (int z = startZ; z < startZ + 16; z++) {
        replaceTreeBlocksInColumn(x, z, limitedRegion, tree);
      }
    }
  }

  private void replaceTreeBlocksInColumn(int x, int z, LimitedRegion limitedRegion, CustomTree tree) {
    for (int y = tree.getMinLevel(); y <= tree.getMaxLevel(); y++) {
      if (!limitedRegion.isInRegion(x, y, z)) continue;
      if (!tree.biomes.contains(limitedRegion.getBiome(x, y, z))) continue;

      Material currentMaterial = limitedRegion.getType(x, y, z);

      if (isLog(currentMaterial) && tree.getLog().getBlockData() != null) {
        limitedRegion.setBlockData(x, y, z, tree.getLog().getBlockData());
      } else if (isLeaves(currentMaterial) && tree.getLeaves().getBlockData() != null) {
        limitedRegion.setBlockData(x, y, z, tree.getLeaves().getBlockData());
      }
    }
  }

  private boolean isLog(Material material) {
    return material.toString().endsWith("_LOG");
  }

  private boolean isLeaves(Material material) {
    return material.toString().endsWith("_LEAVES");
  }
}
