package zone.vao.nexoAddon.items.mechanics;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.BlockUtil;

public record BlockAura(Particle particle, String xOffset, String yOffset, String zOffset, int amount, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {

  public static class BlockAuraListener implements Listener {

    @EventHandler
    public static void onBlockBreak(NexoBlockBreakEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getBlockAura() == null) return;
      Location location = event.getBlock().getLocation();
      if(!event.isCancelled()) {
        BlockUtil.stopBlockAura(location);
      }
    }

    @EventHandler
    public static void onFurnitureBreak(NexoFurnitureBreakEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getBlockAura() == null) return;
      Location location = event.getBaseEntity().getLocation();
      if(!event.isCancelled()) {
        BlockUtil.stopBlockAura(location);
      }
    }

    @EventHandler
    public static void onBlockPlace(NexoBlockPlaceEvent event) {

      handle(event);
    }

    @EventHandler
    public static void onFurniturePlace(NexoFurniturePlaceEvent event) {

      handle(event);
    }

    private static void handle(final NexoFurniturePlaceEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getBlockAura() == null) return;
      Particle particle = mechanics.getBlockAura().particle();
      Location location = event.getBaseEntity().getLocation().clone();
      String xOffsetRange = mechanics.getBlockAura().xOffset();
      String yOffsetRange = mechanics.getBlockAura().yOffset();
      String zOffsetRange = mechanics.getBlockAura().zOffset();
      int amount = mechanics.getBlockAura().amount();
      double deltaX = mechanics.getBlockAura().deltaX();
      double deltaY = mechanics.getBlockAura().deltaY();
      double deltaZ = mechanics.getBlockAura().deltaZ();
      double speed = mechanics.getBlockAura().speed();
      boolean force = mechanics.getBlockAura().force();

      FurnitureMechanic furnitureMechanic = event.getMechanic();
      if (furnitureMechanic != null) {
        BlockUtil.startBlockAura(particle, location, xOffsetRange, yOffsetRange, zOffsetRange, amount, deltaX, deltaY, deltaZ, speed, force);
        PersistentDataContainer pdc = event.getBaseEntity().getPersistentDataContainer();
        pdc.set(new NamespacedKey(NexoAddon.getInstance(), "blockAura"), PersistentDataType.STRING, furnitureMechanic.getItemID());
      }
    }

    private static void handle(final NexoBlockPlaceEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getBlockAura() == null) return;
      Particle particle = mechanics.getBlockAura().particle();
      Location location = event.getBlock().getLocation();
      String xOffsetRange = mechanics.getBlockAura().xOffset();
      String yOffsetRange = mechanics.getBlockAura().yOffset();
      String zOffsetRange = mechanics.getBlockAura().zOffset();
      int amount = mechanics.getBlockAura().amount();
      double deltaX = mechanics.getBlockAura().deltaX();
      double deltaY = mechanics.getBlockAura().deltaY();
      double deltaZ = mechanics.getBlockAura().deltaZ();
      double speed = mechanics.getBlockAura().speed();
      boolean force = mechanics.getBlockAura().force();

      CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(location);

      if (!event.isCancelled() && customBlockMechanic != null) {
        BlockUtil.startBlockAura(particle, location, xOffsetRange, yOffsetRange, zOffsetRange, amount, deltaX, deltaY, deltaZ, speed, force);
        CustomBlockData customBlockData = new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
        customBlockData.set(new NamespacedKey(NexoAddon.getInstance(), "blockAura"), PersistentDataType.STRING, customBlockMechanic.getItemID());
      }
    }

    @EventHandler
    public static void onLoad(ChunkLoadEvent event){

      BlockUtil.restartBlockAura(event.getChunk());
    }
  }

}