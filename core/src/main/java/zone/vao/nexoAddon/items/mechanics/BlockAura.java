package zone.vao.nexoAddon.items.mechanics;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockPlaceEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
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
    public static void onBlockPlace(NexoNoteBlockPlaceEvent event) {

      handle(event);
    }

    @EventHandler
    public static void onBlockPlace(NexoStringBlockPlaceEvent event) {

      handle(event);
    }

    @EventHandler
    public static void onBlockPlace(NexoChorusBlockPlaceEvent event) {

      handle(event);
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

      if (!event.isCancelled()) {
        BlockUtil.startBlockAura(particle, location, xOffsetRange, yOffsetRange, zOffsetRange, amount, deltaX, deltaY, deltaZ, speed, force);
        CustomBlockData customBlockData = new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
        customBlockData.set(new NamespacedKey(NexoAddon.getInstance(), "blockAura"), PersistentDataType.STRING, NexoBlocks.customBlockMechanic(location).getItemID());
      }
    }

    @EventHandler
    public static void onLoad(ChunkLoadEvent event){

      BlockUtil.restartBlockAura(event.getChunk());
    }
  }

}