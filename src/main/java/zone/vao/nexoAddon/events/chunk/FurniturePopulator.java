package zone.vao.nexoAddon.events.chunk;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.world.ChunkLoadEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.populators.orePopulator.Ore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FurniturePopulator {

  public static List<Ore> furniturePopulators = new ArrayList<>();

  public static void onLoad(ChunkLoadEvent event){
    World world = event.getWorld();
    Chunk chunk = event.getChunk();

    furniturePopulators = NexoAddon.getInstance()
        .getOrePopulator()
        .getOres()
        .stream()
        .filter(ore -> ore.getNexoFurniture() != null)
        .toList();

    if (furniturePopulators.isEmpty() || ((!event.isNewChunk()) && event.getChunk().isGenerated())) return;

    furniturePopulators.forEach(ore -> processOre(world, chunk, ore));
  }

  public static void processOre(
      World world,
      Chunk chunk,
      Ore ore
  ) {
    if (!ore.getWorlds().contains(world) && !ore.getWorldNames().contains("all")) return;

    NexoAddon.instance.foliaLib.getScheduler().runAsync(populate -> {
      Random random = new Random();
      if (random.nextDouble() > ore.getChance()) return;

      int successfulPlacements = 0;
      int maxRetries;
      if(ore.getIterations() instanceof String str){
        String[] parts = str.split("-");
        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());
        maxRetries = (random.nextInt(max - min + 1) + min) * 20;
      }else{
        maxRetries = (int) ore.getIterations() * 20;
      }

      for (int attempt = 0; attempt < maxRetries && successfulPlacements < (maxRetries/20); attempt++) {
        Location loc = getRandomLocation(chunk, random, ore);

        if (!isValidBiome(loc, ore)) continue;

        if (canPlaceOre(loc, ore)) {
          successfulPlacements++;
          scheduleOrePlacement(loc, ore, successfulPlacements);
        }
      }
    });
  }

  private static Location getRandomLocation(
      Chunk chunk,
      Random random,
      Ore ore
  ) {
    int x = (chunk.getX() << 4) + random.nextInt(16);
    int z = (chunk.getZ() << 4) + random.nextInt(16);
    int y = ore.getMinLevel() + random.nextInt(ore.getMaxLevel() - ore.getMinLevel() + 1);
    return new Location(chunk.getWorld(), x, y, z);
  }

  private static boolean isValidBiome(
      Location loc,
      Ore ore
  ) {
    return ore.getBiomes().contains(loc.getBlock().getBiome());
  }

  private static boolean canPlaceOre(
      Location loc,
      Ore ore
  ) {
    Material targetBlock = loc.getBlock().getType();
    Material belowBlock = loc.clone().add(0, -1, 0).getBlock().getType();
    Material aboveBlock = loc.clone().add(0, 1, 0).getBlock().getType();

    boolean canReplace = ore.getReplace() != null && ore.getReplace().contains(targetBlock);
    boolean canPlaceOn = canPlaceOn(ore, belowBlock, targetBlock);
    boolean canPlaceBelow = canPlaceBelow(ore, aboveBlock, targetBlock);

    return canReplace || canPlaceOn || canPlaceBelow;
  }

  private static void scheduleOrePlacement(
      Location loc,
      Ore ore,
      int placementIndex
  ) {
    NexoAddon.instance.foliaLib.getScheduler().runLater(() -> {
      if (ore.getReplace() != null && ore.getReplace().contains(loc.getBlock().getType())) {
        loc.getBlock().setType(Material.AIR);
      }
      ore.getNexoFurniture().place(loc);
    }, placementIndex * 5L);
  }

  private static boolean canPlaceOn(
      Ore ore,
      Material belowBlock,
      Material targetBlock
  ){
    if(ore.getPlaceOn() == null) return false;

    if(!ore.getPlaceOn().contains(belowBlock)) return false;

    return !ore.isOnlyAir() || targetBlock.isAir();
  }

  private static boolean canPlaceBelow(
      Ore ore,
      Material aboveBlock,
      Material targetBlock
  ){
    if(ore.getPlaceBelow() == null) return false;

    if(!ore.getPlaceBelow().contains(aboveBlock)) return false;

    return !ore.isOnlyAir() || targetBlock.isAir();
  }
}
