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
      PlacementPosition position = getRandomPlacementPosition(chunkX, chunkZ, limitedRegion, ore, random, worldInfo);

      if(position == null) continue;

      if (random.nextDouble() <= ore.getClusterChance() && ore.getVeinSize() > 0 && ore.getClusterChance() > 0.0) {
        successfulPlacements += generateVein(worldInfo, random, limitedRegion, position, ore);
      } else {
        if (canReplaceBlock(position, ore)) {
          placeBlock(position, ore, worldInfo, limitedRegion);
          successfulPlacements++;
        } else if (canPlaceOnBlock(position, ore, limitedRegion)) {
          placeBlock(position.above(), ore, worldInfo, limitedRegion);
          successfulPlacements++;
        } else if (canPlaceBelowBlock(position, ore, limitedRegion)) {
          placeBlock(position.below(), ore, worldInfo, limitedRegion);
          successfulPlacements++;
        }
      }
    }
  }

  private int generateVein(WorldInfo worldInfo, Random random, LimitedRegion limitedRegion, PlacementPosition start, Ore ore) {
    int veinSize = ore.getVeinSize();
    int placedBlocks = 0;

    if (ore.isPillar()) {
      for (int i = 0; i < veinSize; i++) {
        Location loc = new Location(limitedRegion.getWorld(), start.x(), start.y() - i, start.z());
        PlacementPosition pillarPos = new PlacementPosition(worldInfo, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), limitedRegion.getType(loc), limitedRegion.getBiome(loc), limitedRegion);
        if (canPlaceBelowBlock(pillarPos.above(), ore, limitedRegion) || placedBlocks > 0 && !ore.getPlaceBelow().isEmpty()) {
          if (!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), pillarPos.x(), pillarPos.y(), pillarPos.z())))
            continue;
          placeBlock(pillarPos, ore, worldInfo, limitedRegion);
          placedBlocks++;
        }
      }
      return placedBlocks;
    }

    for (int i = 0; i < veinSize; i++) {
      PlacementPosition nextPosition = getAdjacentPlacementPosition(start, random, limitedRegion, ore, placedBlocks > 0 && !ore.getPlaceBelow().isEmpty());
      if (nextPosition == null) break;

      if (canReplaceBlock(nextPosition, ore)) {
        if (!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y(), nextPosition.z())))
          continue;
        placeBlock(nextPosition, ore, worldInfo, limitedRegion);
        placedBlocks++;
      } else if (canPlaceOnBlock(nextPosition, ore, limitedRegion)) {
        if (!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y()+1, nextPosition.z())))
          continue;
        placeBlock(nextPosition.above(), ore, worldInfo, limitedRegion);
        placedBlocks++;
      } else if (canPlaceBelowBlock(nextPosition, ore, limitedRegion) || placedBlocks > 0 && !ore.getPlaceBelow().isEmpty()) {
        if (!ore.isPillar()) {
          if (!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y()-1, nextPosition.z())))
            continue;
          placeBlock(nextPosition.below(), ore, worldInfo, limitedRegion);
          placedBlocks++;
        } else {
          if (!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y(), nextPosition.z())))
            continue;
          placeBlock(nextPosition, ore, worldInfo, limitedRegion);
          placedBlocks++;
        }
      } else {
        break;
      }

      start = nextPosition;
    }
    return placedBlocks;
  }

  private PlacementPosition getAdjacentPlacementPosition(PlacementPosition start, Random random, LimitedRegion limitedRegion, Ore ore, boolean below) {
    int xOffset = random.nextInt(3) - 1;
    int yOffset = below ? -1 : (random.nextInt(3) - 1);
    int zOffset = random.nextInt(3) - 1;

    int x = start.x() + xOffset;
    int y = start.y() + yOffset;
    int z = start.z() + zOffset;

    if (!limitedRegion.isInRegion(x, y, z)) return null;

    Material blockType = limitedRegion.getType(x, y, z);
    Biome biome = limitedRegion.getBiome(x, y, z);

    return new PlacementPosition(start.worldInfo, x, y, z, blockType, biome, limitedRegion);
  }


  private PlacementPosition getRandomPlacementPosition(int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore, Random random, WorldInfo worldInfo) {
    int x = (chunkX << 4) + random.nextInt(16);
    int z = (chunkZ << 4) + random.nextInt(16);
    int y = ore.getMinLevel() + random.nextInt(ore.getMaxLevel() - ore.getMinLevel() + 1);

    if (!limitedRegion.isInRegion(x, y, z)) return null;

    Material blockType = limitedRegion.getType(x, y, z);
    Biome biome = limitedRegion.getBiome(x, y, z);

    return new PlacementPosition(worldInfo,  x, y, z, blockType, biome, limitedRegion);
  }

  private boolean canReplaceBlock(PlacementPosition position, Ore ore) {
    return ore.getReplace() != null
        && position != null
        && ore.getReplace().contains(position.blockType())
        && ore.getBiomes().contains(position.biome());
  }

  private boolean canPlaceOnBlock(PlacementPosition position, Ore ore, LimitedRegion limitedRegion) {
    if(!limitedRegion.isInRegion(position.x(), position.y() + 1, position.z())) return false;
    Material aboveBlockType = limitedRegion.getType(position.x(), position.y() + 1, position.z());
    return ore.getPlaceOn() != null
        && ore.getPlaceOn().contains(position.blockType())
        && ore.getBiomes().contains(position.biome())
        && (!ore.isOnlyAir() || aboveBlockType.isAir());
  }

  private boolean canPlaceBelowBlock(PlacementPosition position, Ore ore, LimitedRegion limitedRegion) {
    if(!limitedRegion.isInRegion(position.x(), position.y() - 1, position.z())) return false;
    Material belowBlockType = limitedRegion.getType(position.x(), position.y() - 1, position.z());
    return (!ore.getPlaceBelow().isEmpty())
        && ore.getPlaceBelow().contains(position.blockType())
        && ore.getBiomes().contains(position.biome())
        && (!ore.getPlaceBelow().contains(belowBlockType))
        && (!ore.isOnlyAir() || belowBlockType.isAir());
  }



  private void placeBlock(PlacementPosition position, Ore ore, WorldInfo worldInfo, LimitedRegion limitedRegion) {
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

  public record PlacementPosition(WorldInfo worldInfo, int x, int y, int z, Material blockType, Biome biome, LimitedRegion limitedRegion) {

    PlacementPosition above() {
      return new PlacementPosition(worldInfo, x, y + 1, z, limitedRegion.getType( x, y + 1, z), biome, limitedRegion);
    }

    PlacementPosition below() {
      return new PlacementPosition(worldInfo,x, y - 1, z, limitedRegion.getType(x, y - 1, z), biome, limitedRegion);
    }

    Location getLocation() {
      return new Location(Bukkit.getWorld(worldInfo.getUID()), x, y, z);
    }
  }
}