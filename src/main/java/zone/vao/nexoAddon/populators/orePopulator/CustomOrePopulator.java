package zone.vao.nexoAddon.populators.orePopulator;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomOrePopulator extends BlockPopulator {

  private final OrePopulator orePopulator;

  private final Map<String, BlockData> cachedBlocks = new HashMap<>();
  public CustomOrePopulator(OrePopulator orePopulator) {
    this.orePopulator = orePopulator;
  }

  @Override
  public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
    for (Ore ore : orePopulator.getOres()) {
      if(ore.getNexoFurniture() != null || !ore.worldNames.contains(worldInfo.getName())) continue;
      if(ore.getIterations() instanceof Integer iterations && iterations < 0)
        replaceBlocks(worldInfo, random, chunkX, chunkZ, limitedRegion, ore);
      else
        generateOre(worldInfo, random, chunkX, chunkZ, limitedRegion, ore);
    }
  }

  private void replaceBlocks(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore){
    List<Material> toReplace = ore.getReplace();
    int startX = chunkX << 4;
    int startZ = chunkZ << 4;
    boolean replaceAir = !toReplace.contains(Material.AIR);

    for (int x = startX; x < startX + 16; x++) {
      for (int z = startZ; z < startZ + 16; z++) {

        for (int y = worldInfo.getMinHeight(); y <= (replaceAir ? limitedRegion.getHighestBlockYAt(x,z) : worldInfo.getMaxHeight()); y++) {
          if (!limitedRegion.isInRegion(x, y, z)) continue;
          if (!ore.biomes.isEmpty() && !ore.biomes.contains(getBiomeKeyAtLocation(new Location(Bukkit.getWorld(worldInfo.getUID()), x, y, z)))) continue;

          Material currentMaterial = limitedRegion.getType(x, y, z);

          if (toReplace.contains(currentMaterial)) {
            PlacementPosition position = new PlacementPosition(worldInfo, x, y, z, currentMaterial, limitedRegion.getBiome(x, y, z), limitedRegion);
            placeBlock(position, ore, worldInfo, limitedRegion);
          }
        }
      }
    }
  }

  private void generateOre(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore) {
    if (random.nextDouble() > ore.getChance()) return;

    int attempts;
    if(ore.getIterations() instanceof String str){
      String[] parts = str.split("-");
      int min = Integer.parseInt(parts[0].trim());
      int max = Integer.parseInt(parts[1].trim());
      attempts = random.nextInt(max - min + 1) + min;
    }else{
      attempts = (int) ore.getIterations();
    }
    int successfulPlacements = 0;
    int totalAttempts = 0;
    int maxRetries = attempts * 80;

    while (successfulPlacements < attempts && totalAttempts < maxRetries) {
      totalAttempts++;
      PlacementPosition position = getRandomPlacementPosition(chunkX, chunkZ, limitedRegion, ore, random, worldInfo);

      if(position == null) continue;
      int veinSize;
      if(ore.getVeinSize() instanceof String str){
        String[] parts = str.split("-");
        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());
        veinSize = random.nextInt(max - min + 1) + min;
      }else{
        veinSize = (int) ore.getVeinSize();
      }
      if (random.nextDouble() <= ore.getClusterChance() && veinSize > 0 && ore.getClusterChance() > 0.0) {
        successfulPlacements += generateVein(worldInfo, random, limitedRegion, position, ore, veinSize);
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

  private int generateVein(WorldInfo worldInfo, Random random, LimitedRegion limitedRegion, PlacementPosition start, Ore ore, int veinSize) {

    int placedBlocks = 0;

    for (int i = 0; i < veinSize; i++) {
      PlacementPosition nextPosition = getAdjacentPlacementPosition(start, random, limitedRegion, ore, placedBlocks > 0 && !ore.getPlaceBelow().isEmpty());

      if (nextPosition == null) break;

//      Bukkit.broadcastMessage(ore.id+" "+limitedRegion.getCenterChunkX()+","+limitedRegion.getCenterChunkZ()+" "+placedBlocks+1);
//      Bukkit.broadcastMessage("  "+nextPosition.x+" "+nextPosition.y+" "+nextPosition.z);

      if (canReplaceBlock(nextPosition, ore)) {
        if(!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y(), nextPosition.z())))
          continue;
        placeBlock(nextPosition, ore, worldInfo, limitedRegion);
        placedBlocks++;
      } else if (canPlaceOnBlock(nextPosition, ore, limitedRegion)) {
        if(!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y()+1, nextPosition.z())))
          continue;
        placeBlock(nextPosition.above(), ore, worldInfo, limitedRegion);
        placedBlocks++;
      } else if( canPlaceBelowBlock(nextPosition, ore, limitedRegion) || placedBlocks > 0 && !ore.getPlaceBelow().isEmpty()) {
        if(!limitedRegion.isInRegion(new Location(Bukkit.getWorld(worldInfo.getUID()), nextPosition.x(), nextPosition.y()-1, nextPosition.z())))
          continue;
        placeBlock(nextPosition.below(), ore, worldInfo, limitedRegion);
        placedBlocks++;
      } else {
        break;
      }

      start = nextPosition;
    }

    return placedBlocks;
  }

  private PlacementPosition getAdjacentPlacementPosition(PlacementPosition start, Random random, LimitedRegion limitedRegion, Ore ore, boolean below) {
    Set<String> checkedLocations = new HashSet<>();
    int attempts = 0;

    try {
      for (int i = 0; i < 10 && attempts < 100; i++) {
        attempts++;
        int xOffset = random.nextInt(3) - 1;
        int yOffset = below ? -1 : (random.nextInt(3) - 1);
        int zOffset = random.nextInt(3) - 1;

        int x = start.x() + xOffset;
        int y = start.y() + yOffset;
        int z = start.z() + zOffset;

        String key = x + "," + y + "," + z;
        if (checkedLocations.contains(key)) continue;
        checkedLocations.add(key);
        if (!limitedRegion.isInRegion(x, y, z))
          continue;

        Material blockType = limitedRegion.getType(x, y, z);

        if ((ore.getPlaceOn().contains(blockType) && (!ore.isOnlyAir() || !blockType.isAir())) || ore.getReplace().contains(blockType) || (ore.getPlaceBelow().contains(blockType) && (!ore.isOnlyAir() || !blockType.isAir()))) {
          return new PlacementPosition(start.worldInfo, x, y, z, blockType, start.biome(), limitedRegion);
        }
      }
      return null;
    }catch(Exception ignored){
      return null;
    }
  }

  private PlacementPosition getRandomPlacementPosition(int chunkX, int chunkZ, LimitedRegion limitedRegion, Ore ore, Random random, WorldInfo worldInfo) {
    try {
      int x = (chunkX << 4) + random.nextInt(16);
      int z = (chunkZ << 4) + random.nextInt(16);
      int y = ore.getMinLevel() + random.nextInt(ore.getMaxLevel() - ore.getMinLevel() + 1);

      if (!limitedRegion.isInRegion(x, y, z))
        return null;

      Material blockType = limitedRegion.getType(x, y, z);
      Biome biome = limitedRegion.getBiome(x, y, z);

      return new PlacementPosition(worldInfo, x, y, z, blockType, biome, limitedRegion);
    }catch(Exception ignored){
      return null;
    }
  }

  private boolean canReplaceBlock(PlacementPosition position, Ore ore) {
    return ore.getReplace() != null
        && position != null
        && ore.getReplace().contains(position.blockType())
        && ore.getBiomes().contains(getBiomeKeyAtLocation(new Location(Bukkit.getWorld(position.worldInfo().getUID()), position.x, position.y, position.z)));
  }

  private boolean canPlaceOnBlock(PlacementPosition position, Ore ore, LimitedRegion limitedRegion) {
    if(!limitedRegion.isInRegion(position.x(), position.y() + 1, position.z())) return false;
    Material aboveBlockType = limitedRegion.getType(position.x(), position.y() + 1, position.z());
    return ore.getPlaceOn() != null
        && ore.getPlaceOn().contains(position.blockType())
        && ore.getBiomes().contains(getBiomeKeyAtLocation(new Location(Bukkit.getWorld(position.worldInfo().getUID()), position.x, position.y, position.z)))
        && (!ore.isOnlyAir() || aboveBlockType.isAir());
  }

  private boolean canPlaceBelowBlock(PlacementPosition position, Ore ore, LimitedRegion limitedRegion) {
    if(!limitedRegion.isInRegion(position.x(), position.y() - 1, position.z())) return false;
    Material belowBlockType = limitedRegion.getType(position.x(), position.y() - 1, position.z());
    return (!ore.getPlaceBelow().isEmpty())
        && ore.getPlaceBelow().contains(position.blockType())
        && ore.getBiomes().contains(getBiomeKeyAtLocation(new Location(Bukkit.getWorld(position.worldInfo().getUID()), position.x, position.y, position.z)))
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
    if(position.below().blockType.equals(Material.GRASS_BLOCK) && !limitedRegion.getBlockData(position.below().x, position.below().y, position.below().z).equals(Material.GRASS_BLOCK.createBlockData())){
      limitedRegion.setBlockData(position.below().x(), position.below().y(), position.below().z(), Material.GRASS_BLOCK.createBlockData());
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

  public static NamespacedKey getBiomeKeyAtLocation(Location loc) {
    ServerLevel nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
    int biomeX = loc.getBlockX() >> 2;
    int biomeY = loc.getBlockY() >> 2;
    int biomeZ = loc.getBlockZ() >> 2;
    net.minecraft.world.level.biome.Biome biomeBase = nmsWorld.getNoiseBiome(biomeX, biomeY, biomeZ).value();
    ResourceLocation biomeId = CraftRegistry.getMinecraftRegistry(Registries.BIOME).getKey(biomeBase);
    return new NamespacedKey(biomeId.getNamespace(), biomeId.getPath());
  }

}