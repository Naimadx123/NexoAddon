package zone.vao.nexoAddon.populators;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.persistence.PersistentDataType;

public record WorldPopulationRegion(World world, int chunkX, int chunkZ) implements PopulationRegion {

  @Override
  public boolean contains(int x, int y, int z) {
    if (y < world.getMinHeight() || y >= world.getMaxHeight()) return false;
    if (!Bukkit.isOwnedByCurrentRegion(new Location(world, x, y, z))) return false;
    return (x >> 4) == chunkX && (z >> 4) == chunkZ || world.isChunkLoaded(x >> 4, z >> 4);
  }

  @Override
  public Material getType(int x, int y, int z) {
    return world.getBlockAt(x, y, z).getType();
  }

  @Override
  public Biome getBiome(int x, int y, int z) {
    return world.getBiome(x, y, z);
  }

  @Override
  public void setBiome(int x, int y, int z, Biome biome) {
    world.setBiome(x, y, z, biome);
  }

  @Override
  public boolean place(PopulatorItem item, int x, int y, int z, BlockFace facing) {
    Location location = new Location(world, x, y, z);
    if (item.furniture() != null) {
      Block block = world.getBlockAt(x, y, z);
      BlockData original = block.getBlockData();
      boolean placed = false;
      try {
        if (!original.getMaterial().isAir()) block.setType(Material.AIR, false);
        placed = item.furniture().place(location, yaw(facing), facing) != null;
      } finally {
        if (!placed) block.setBlockData(original, false);
      }
      if (!placed) return false;
    } else if (item.saplingTime() != null) {
      NexoBlocks.place(item.id(), location);
      CustomBlockMechanic block = NexoBlocks.customBlockMechanic(location.getBlock());
      if (block == null) return false;
      StringBlockMechanic sapling = NexoBlocks.stringMechanic(block.getItemID());
      if (sapling != null && sapling.isSapling()) {
        CustomBlockData data = new CustomBlockData(location.getBlock(), NexoPlugin.instance());
        data.set(new NamespacedKey(NexoPlugin.instance(), "sapling"), PersistentDataType.INTEGER, sapling.sapling().getNaturalGrowthTime());
      }
    } else {
      world.getBlockAt(x, y, z).setBlockData(item.blockData(facing), false);
      if (item.upperData() != null) world.getBlockAt(x, y + 1, z).setBlockData(item.upperData().clone(), false);
    }
    return true;
  }

  private float yaw(BlockFace facing) {
    return switch (facing) {
      case NORTH -> 180f;
      case EAST -> -90f;
      case WEST -> 90f;
      default -> 0f;
    };
  }
}
