package zone.vao.nexoAddon.classes.populators.orePopulator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CustomOrePopulator extends BlockPopulator {

  private final OrePopulator orePopulator;

  public CustomOrePopulator(OrePopulator orePopulator) {
    this.orePopulator = orePopulator;
  }

  @Override
  public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
    for (Ore ore : orePopulator.getOres()) {
      if(ore.getNexoFurniture() != null) continue;
      generateOre(worldInfo, random, chunkX, chunkZ, limitedRegion, ore);
    }
  }

  private void generateOre(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore) {
    if (random.nextDouble() > ore.getChance()) return;

    int attempts = ore.getIterations();
    int successfulPlacements = 0;
    int totalAttempts = 0;
    int maxRetries = attempts * 80;

    while (successfulPlacements < attempts && totalAttempts < maxRetries) {
      totalAttempts++;
      PlacementPosition position = getRandomPlacementPosition(chunkX, chunkZ, limitedRegion, ore, random);
      if (position == null) continue;

      if (random.nextDouble() <= ore.getClusterChance() && ore.getVeinSize() > 0 && ore.getClusterChance() > 0.0) {
        successfulPlacements += generateVein(worldInfo, random, limitedRegion, position, ore);
      } else {
        if (canReplaceBlock(position, ore)) {
          placeBlock(position, ore, worldInfo, limitedRegion, false);
          successfulPlacements++;
        } else if (canPlaceOnBlock(position, ore, limitedRegion)) {
          placeBlock(position.above(), ore, worldInfo, limitedRegion, true);
          successfulPlacements++;
        }
      }
    }
  }

  private int generateVein(WorldInfo worldInfo, Random random, LimitedRegion limitedRegion, PlacementPosition start, Ore ore) {
    int veinSize = ore.getVeinSize();
    int placedBlocks = 0;

    for (int i = 0; i < veinSize; i++) {
      PlacementPosition nextPosition = getAdjacentPlacementPosition(start, random, limitedRegion, ore);

      if (nextPosition == null) break;

      if (canReplaceBlock(nextPosition, ore)) {
        placeBlock(nextPosition, ore, worldInfo, limitedRegion, false);
        placedBlocks++;
      } else if (canPlaceOnBlock(nextPosition, ore, limitedRegion)) {
        placeBlock(nextPosition.above(), ore, worldInfo, limitedRegion, true);
        placedBlocks++;
      } else {
        break;
      }

      start = nextPosition;
    }

    return placedBlocks;
  }


  private PlacementPosition getAdjacentPlacementPosition(PlacementPosition start, Random random, LimitedRegion limitedRegion, Ore ore) {
    int xOffset = random.nextInt(3) - 1;
    int yOffset = random.nextInt(3) - 1;
    int zOffset = random.nextInt(3) - 1;

    int x = start.x() + xOffset;
    int y = start.y() + yOffset;
    int z = start.z() + zOffset;

    if (!limitedRegion.isInRegion(x, y, z)) return null;

    Material blockType = limitedRegion.getType(x, y, z);
    Biome biome = limitedRegion.getBiome(x, y, z);

    return new PlacementPosition(x, y, z, blockType, biome, limitedRegion);
  }


  private PlacementPosition getRandomPlacementPosition(int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore, Random random) {
    int x = (chunkX << 4) + random.nextInt(16);
    int z = (chunkZ << 4) + random.nextInt(16);
    int y = ore.getMinLevel() + random.nextInt(ore.getMaxLevel() - ore.getMinLevel() + 1);

    if (!limitedRegion.isInRegion(x, y, z)) return null;

    Material blockType = limitedRegion.getType(x, y, z);
    Biome biome = limitedRegion.getBiome(x, y, z);

    return new PlacementPosition(x, y, z, blockType, biome, limitedRegion);
  }

  private boolean canReplaceBlock(PlacementPosition position, Ore ore) {
    return ore.getReplace() != null
        && ore.getReplace().contains(position.blockType())
        && ore.getBiomes().contains(position.biome());
  }

  private boolean canPlaceOnBlock(PlacementPosition position, Ore ore, LimitedRegion limitedRegion) {
    Material aboveBlockType = limitedRegion.getType(position.x(), position.y() + 1, position.z());
    return ore.getPlaceOn() != null
        && ore.getPlaceOn().contains(position.blockType())
        && ore.getBiomes().contains(position.biome())
        && aboveBlockType.isAir();
  }

  private void placeBlock(PlacementPosition position, Ore ore, WorldInfo worldInfo, LimitedRegion limitedRegion, boolean isAbove) {
    if (ore.getNexoBlocks() != null && ore.getNexoBlocks().getBlockData() != null) {
      if(ore.isTall()) {
        limitedRegion.setBlockData(position.x(), position.y(), position.z(), ore.getNexoBlocks().getBlockData());
        World world = Bukkit.getWorld(worldInfo.getUID());
        if(limitedRegion.getType(new Location(world, position.x(), position.y()+1, position.z())).isAir())
          limitedRegion.setBlockData(position.x(), position.y()+1, position.z(), Material.TRIPWIRE.createBlockData());
      }else{
        limitedRegion.setBlockData(position.x(), position.y(), position.z(), ore.getNexoBlocks().getBlockData());
      }
    } else{
      limitedRegion.setBlockData(position.x(), position.y(), position.z(), ore.getVanillaMaterial().createBlockData());
    }
  }

  public record PlacementPosition(int x, int y, int z, Material blockType, Biome biome, LimitedRegion limitedRegion) {

    PlacementPosition above() {
      return new PlacementPosition(x, y + 1, z, limitedRegion.getType(x, y + 1, z), biome, limitedRegion);
    }

    boolean isAirAbove() {
      return limitedRegion.getType(x, y + 1, z).isAir();
    }
  }
}