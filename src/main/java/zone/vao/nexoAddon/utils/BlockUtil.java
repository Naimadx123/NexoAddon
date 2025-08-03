package zone.vao.nexoAddon.utils;

import com.google.common.collect.Sets;
import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureHelpers;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.tcoded.folialib.wrapper.task.WrappedBukkitTask;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.Decay;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockUtil {

  public static final Set<Material> UNBREAKABLE_BLOCKS = Sets.newHashSet(Material.BEDROCK, Material.BARRIER, Material.NETHER_PORTAL, Material.END_PORTAL_FRAME, Material.END_PORTAL, Material.END_GATEWAY);
  private static final Set<Location> processedCustomBlocks = ConcurrentHashMap.newKeySet();
  public static final Set<Location> processedShiftblocks = ConcurrentHashMap.newKeySet();

  public static void startShiftBlock(Location location, CustomBlockMechanic to, CustomBlockMechanic target, int time) {
    World world = location.getWorld();
    if(world == null || processedShiftblocks.contains(location)) return;

    Location finalLocation = location.clone();

    if(time > 0)
      processedShiftblocks.add(location);
    PersistentDataContainer pdc = new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
    pdc.set(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"), PersistentDataType.STRING, target.getItemID());
    finalLocation.getBlock().setType(Material.AIR);
    NexoAddon.instance.foliaLib.getScheduler().runLater(() -> NexoBlocks.place(to.getItemID(), finalLocation), 1);

    if(time <= 0)
      return;
    NexoAddon.instance.foliaLib.getScheduler().runLaterAsync( laterAsync -> {
      if(!NexoBlocks.isCustomBlock(finalLocation.getBlock()) ||
          !NexoBlocks.customBlockMechanic(finalLocation).getItemID().equalsIgnoreCase(to.getItemID())
      ) {
        laterAsync.cancel();
        processedShiftblocks.remove(finalLocation);
        pdc.remove(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"));
        return;
      }
      NexoAddon.instance.foliaLib.getScheduler().runNextTick(replaceToAir -> {
        finalLocation.getBlock().setType(Material.AIR);
      });

      NexoAddon.instance.foliaLib.getScheduler().runLater(() -> NexoBlocks.place(target.getItemID(), finalLocation), 1L);

      processedShiftblocks.remove(finalLocation);
      pdc.remove(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"));
    },time*20L);
  }

  public static void startShiftBlock(ItemDisplay itemDisplay, FurnitureMechanic to, FurnitureMechanic target, int time) {
    if(processedShiftblocks.contains(itemDisplay.getLocation())) return;

    ItemDisplay templateEntity = (ItemDisplay) itemDisplay.copy();

    Location finalLocation = itemDisplay.getLocation().clone();

    if(time > 0)
      processedShiftblocks.add(finalLocation);
    PersistentDataContainer pdc = new CustomBlockData(finalLocation.getBlock(), NexoAddon.getInstance());
    pdc.set(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"), PersistentDataType.STRING, target.getItemID());
    FurnitureMechanic previous = NexoFurniture.furnitureMechanic(finalLocation);
    if(previous == null) return;
    ItemDisplay newOne = to.place(finalLocation, templateEntity.getYaw(), templateEntity.getFacing(), false);
    if(!newOne.getLocation().equals(finalLocation)) {
      newOne.teleport(finalLocation);
    }
    if(FurnitureHelpers.furnitureDye(templateEntity) != null) {
      FurnitureHelpers.furnitureDye(newOne, FurnitureHelpers.furnitureDye(templateEntity));
    }
    NexoAddon.instance.foliaLib.getScheduler().runLater(() -> {
      previous.removeBaseEntity(itemDisplay);
      NexoFurniture.furnitureMechanic(finalLocation).getHitbox().refreshHitboxes(newOne, to);
    }, 3L);

    if(time <= 0)
      return;
    NexoAddon.instance.foliaLib.getScheduler().runLaterAsync(() -> {
      NexoAddon.instance.foliaLib.getScheduler().runNextTick(shiftBlock -> {
        if(!NexoFurniture.isFurniture(finalLocation) ||
            !NexoFurniture.furnitureMechanic(finalLocation).getItemID().equalsIgnoreCase(to.getItemID())
        ) {
          shiftBlock.cancel();
          processedShiftblocks.remove(finalLocation);
          pdc.remove(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"));
          return;
        }

        ItemDisplay oldFurniture = NexoFurniture.baseEntity(finalLocation);
        ItemDisplay original = target.place(finalLocation, templateEntity.getYaw(), templateEntity.getFacing(), false);
        if(FurnitureHelpers.furnitureDye(templateEntity) != null) {
          FurnitureHelpers.furnitureDye(original, FurnitureHelpers.furnitureDye(templateEntity));
        }
        if(oldFurniture != null)
          NexoFurniture.remove(oldFurniture);
        processedShiftblocks.remove(finalLocation);
        pdc.remove(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"));
      });
    }, time*20L);
  }

  public static void startDecay(Location location) {
    int radius = 10;
    World world = location.getWorld();

    if (world == null || VersionUtil.nexoVersionLessThan("0.10.0")) {
      return;
    }

    NexoAddon.instance.foliaLib.getScheduler().runAsync(startDecay -> {
      for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
          for (int z = -radius; z <= radius; z++) {
            Location currentLocation = location.clone().add(x, y, z);
            Block block = currentLocation.getBlock();

            if (processedCustomBlocks.contains(currentLocation)) {
              continue;
            }

            if (NexoBlocks.isCustomBlock(block)) {
              String itemId = NexoBlocks.customBlockMechanic(block.getLocation()).getItemID();
              Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(itemId);

              if (mechanic != null && mechanic.getDecay() != null) {
                Decay decay = mechanic.getDecay();

                processedCustomBlocks.add(currentLocation);
                startDecayTimer(block, decay);
              }
            }
          }
        }
      }
    });
  }

  private static void startDecayTimer(Block block, Decay decay) {
    NexoAddon.instance.foliaLib.getScheduler().runTimerAsync(decayTimer -> {
      if (block.getType() == Material.AIR && !NexoBlocks.isCustomBlock(block)) {
        processedCustomBlocks.remove(block.getLocation());
        decayTimer.cancel();
        return;
      }

      boolean isConnected = isConnectedToBase(block, decay);

      if (!isConnected && Math.random() <= decay.chance()) {
        NexoAddon.instance.foliaLib.getScheduler().runNextTick(removeBlock -> NexoBlocks.remove(block.getLocation()));
        processedCustomBlocks.remove(block.getLocation());
        decayTimer.cancel();
      }else if (isConnected) {
        processedCustomBlocks.remove(block.getLocation());
        decayTimer.cancel();
      }
    }, 0, decay.time() * 20L);
  }

  private static boolean isConnectedToBase(Block startBlock, Decay decay) {
    // Check if the block itself is a base
    if (isBaseBlock(startBlock, decay)) {
      return true;
    }

    String originalBlockId = NexoBlocks.isCustomBlock(startBlock) ?
            NexoBlocks.customBlockMechanic(startBlock.getLocation()).getItemID() : null;

    if (originalBlockId == null) {
      return false;
    }

    // A* algorithm implementation
    Set<Location> visited = new HashSet<>();
    PriorityQueue<BlockNode> openSet = new PriorityQueue<>();

    // Add starting block to open set
    openSet.add(new BlockNode(startBlock, 0));

    while (!openSet.isEmpty()) {
      BlockNode current = openSet.poll();

      if (visited.contains(current.block.getLocation())) {
        continue;
      }

      visited.add(current.block.getLocation());

      if (isBaseBlock(current.block, decay)) {
        return true;
      }

      if (current.distance >= decay.radius()) {
        continue;
      }

      int[][] directions = {{0,1,0}, {0,-1,0}, {1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}};
      for (int[] dir : directions) {
        Block adjacent = current.block.getWorld().getBlockAt(
                current.block.getX() + dir[0],
                current.block.getY() + dir[1],
                current.block.getZ() + dir[2]
        );

        if (visited.contains(adjacent.getLocation())) {
          continue;
        }

        // Only continue with same type blocks or base blocks
        if (isBaseBlock(adjacent, decay) ||
                (NexoBlocks.isCustomBlock(adjacent) &&
                        NexoBlocks.customBlockMechanic(adjacent.getLocation()) != null &&
                        originalBlockId.equals(NexoBlocks.customBlockMechanic(adjacent.getLocation()).getItemID()))) {

          int newDistance = current.distance + 1;
          openSet.add(new BlockNode(adjacent, newDistance));
        }
      }
    }

    return false;
  }

  // Helper class for A* algorithm
  private static class BlockNode implements Comparable<BlockNode> {
    Block block;
    int distance;

    public BlockNode(Block block, int distance) {
      this.block = block;
      this.distance = distance;
    }

    @Override
    public int compareTo(BlockNode other) {
      return Integer.compare(this.distance, other.distance);
    }
  }

  private static boolean isBaseBlock(Block block, Decay decay) {
    return decay.base().contains(block.getType()) ||
            (NexoBlocks.isCustomBlock(block) &&
                    NexoBlocks.customBlockMechanic(block.getLocation()) != null &&
                    decay.nexoBase().contains(NexoBlocks.customBlockMechanic(block.getLocation()).getItemID()));
  }

  public static void startBlockAura(Particle particle, Location location, String xOffsetRange, String yOffsetRange, String zOffsetRange, int amount, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
    WrappedTask task = new WrappedBukkitTask(new BukkitRunnable() {
      @Override
      public void run() {
        World world = location.getWorld();
        if (!NexoBlocks.isCustomBlock(location.getBlock())) {
          cancel();
          stopBlockAura(location);
          return;
        }
        if (world != null) {
          double xOffset = RandomRangeUtil.parseAndGetRandomValue(xOffsetRange);
          double yOffset = RandomRangeUtil.parseAndGetRandomValue(yOffsetRange);
          double zOffset = RandomRangeUtil.parseAndGetRandomValue(zOffsetRange);

          world.spawnParticle(
              particle,
              location.clone().add(xOffset, yOffset, zOffset),
              amount,
              deltaX, deltaY, deltaZ,
              speed,
              null,
              force
          );
        }
      }
    }.runTaskTimerAsynchronously(NexoAddon.getInstance(), 0L, NexoAddon.getInstance().getGlobalConfig().getLong("aura_mechanic_delay", 10)));

    NexoAddon.getInstance().getParticleTasks().put(location, task);
  }

  public static void stopBlockAura(Location location) {
    WrappedTask task = NexoAddon.getInstance().getParticleTasks().remove(location);
    CustomBlockData customBlockData =  new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
    customBlockData.remove(new NamespacedKey(NexoAddon.getInstance(), "blockAura"));
    if (task != null && task.isCancelled()) {
      task.cancel();
    }
  }

  public static void restartBlockAura(Chunk chunk){
    for(Block block : CustomBlockData.getBlocksWithCustomData(NexoAddon.getInstance(), chunk)){
      CustomBlockData customBlockData = new CustomBlockData(block, (NexoAddon.getInstance()));
      if(!customBlockData.has(new NamespacedKey(NexoAddon.getInstance(), "blockAura"), PersistentDataType.STRING)) continue;
      if(!NexoBlocks.isCustomBlock(block)){
        customBlockData.clear();
        continue;
      }

      if(NexoAddon.getInstance().getMechanics().isEmpty()) continue;

      if(NexoAddon.getInstance().getParticleTasks().containsKey(block.getLocation())) continue;
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(NexoBlocks.customBlockMechanic(block.getLocation()).getItemID());
      if (mechanics == null || mechanics.getBlockAura() == null) return;
      Particle particle = mechanics.getBlockAura().particle();
      Location location = block.getLocation();
      String xOffsetRange = mechanics.getBlockAura().xOffset();
      String yOffsetRange = mechanics.getBlockAura().yOffset();
      String zOffsetRange = mechanics.getBlockAura().zOffset();
      int amount = mechanics.getBlockAura().amount();
      double deltaX = mechanics.getBlockAura().deltaX();
      double deltaY = mechanics.getBlockAura().deltaY();
      double deltaZ = mechanics.getBlockAura().deltaZ();
      double speed = mechanics.getBlockAura().speed();
      boolean force = mechanics.getBlockAura().force();
      BlockUtil.startBlockAura(particle, location, xOffsetRange, yOffsetRange, zOffsetRange, amount, deltaX, deltaY, deltaZ, speed, force);
    }
  }
}
