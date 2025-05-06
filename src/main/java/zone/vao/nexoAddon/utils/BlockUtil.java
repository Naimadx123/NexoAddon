package zone.vao.nexoAddon.utils;

import com.google.common.collect.Sets;
import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
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
        target.place(finalLocation, templateEntity.getYaw(), templateEntity.getFacing(), false);
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

    NexoAddon.instance.foliaLib.getScheduler().runAtLocation(location, startDecay -> {
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

      boolean hasBaseNearby = false;
      for (int x = -decay.radius(); x <= decay.radius(); x++) {
        for (int y = -decay.radius(); y <= decay.radius(); y++) {
          for (int z = -decay.radius(); z <= decay.radius(); z++) {
            Block nearbyBlock = block.getLocation().add(x, y, z).getBlock();
            if (decay.base().contains(nearbyBlock.getType())
                || NexoBlocks.customBlockMechanic(nearbyBlock.getLocation()) != null
                && decay.nexoBase().contains(NexoBlocks.customBlockMechanic(nearbyBlock.getLocation()).getItemID())
            ) {
              hasBaseNearby = true;
              break;
            }
          }
        }
        if (hasBaseNearby) break;
      }

      if (!hasBaseNearby && Math.random() <= decay.chance()) {
        NexoAddon.instance.foliaLib.getScheduler().runNextTick(removeBlock -> NexoBlocks.remove(block.getLocation()));

        processedCustomBlocks.remove(block.getLocation());
        decayTimer.cancel();
      }
    }, 0, decay.time() * 20L);
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