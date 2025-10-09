package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.nexomc.nexo.utils.blocksounds.BlockSounds;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import com.nexomc.protectionlib.ProtectionLib;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.InventoryUtil;

import java.util.*;

import static zone.vao.nexoAddon.utils.BlockUtil.startShiftBlock;

public record Stackable(String next, String group) {

  public static class StackableListener implements Listener {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public static void onStackable(final NexoStringBlockInteractEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      String itemId = NexoItems.idFromItem(event.getItemInHand());
      if(itemId == null) return;
      String StringBlockId = event.getMechanic().getItemID();

      Mechanics mechanicsItem = NexoAddon.getInstance().getMechanics().get(itemId);
      Mechanics mechanicsStringBlock = NexoAddon.getInstance().getMechanics().get(StringBlockId);
      if(mechanicsStringBlock == null || mechanicsStringBlock.getStackable() == null
          || mechanicsItem == null || mechanicsItem.getStackable() == null
      ) return;

      if(!mechanicsStringBlock.getStackable().group().equalsIgnoreCase(mechanicsItem.getStackable().group())
          || !ProtectionLib.canBuild(event.getPlayer(), event.getBlock().getLocation())
      ) return;

      String nextStage = mechanicsStringBlock.getStackable().next();
      StringBlockMechanic newBlock = NexoBlocks.stringMechanic(nextStage);
      if(newBlock == null) return;

      event.setCancelled(true);
      List<Loot> loots = new ArrayList<>();
      Drop drop = new Drop(loots, false, false, newBlock.getItemID());
      NexoBlocks.remove(event.getBlock().getLocation(), null, drop);
      NexoBlocks.place(nextStage, event.getBlock().getLocation());

      BlockSounds blockSounds = newBlock.getBlockSounds();
      if(blockSounds != null && blockSounds.hasPlaceSound()){
        event.getBlock().getWorld().playSound(event.getBlock().getLocation(), blockSounds.getPlaceSound(), SoundCategory.BLOCKS, blockSounds.getPlaceVolume(), blockSounds.getPlacePitch());
      }

      event.getPlayer().swingMainHand();
      if(!event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
        InventoryUtil.removePartialStack(event.getPlayer(), event.getItemInHand(), 1);
    }

    @EventHandler
    public static void onStackableFurniture(final NexoFurnitureInteractEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      String itemId = NexoItems.idFromItem(event.getItemInHand());
      if(itemId == null) return;
      String furnitureId = event.getMechanic().getItemID();

      Mechanics mechanicsItem = NexoAddon.getInstance().getMechanics().get(itemId);
      Mechanics mechanicsFurniture = NexoAddon.getInstance().getMechanics().get(furnitureId);
      if(mechanicsFurniture == null || mechanicsFurniture.getStackable() == null
          || mechanicsItem == null || mechanicsItem.getStackable() == null
      ) return;

      if(!mechanicsFurniture.getStackable().group().equalsIgnoreCase(mechanicsItem.getStackable().group())
          || !ProtectionLib.canBuild(event.getPlayer(), event.getBaseEntity().getLocation())
      ) return;

      String nextStage = mechanicsFurniture.getStackable().next();
      FurnitureMechanic newBlock = NexoFurniture.furnitureMechanic(nextStage);
      if(newBlock == null) return;
      UUID uuid = event.getBaseEntity().getUniqueId();
      if (hasCooldown(uuid)) return;
      setCooldown(uuid, 3);

      event.setCancelled(true);

      startShiftBlock(event.getBaseEntity(), NexoFurniture.furnitureMechanic(nextStage), event.getMechanic(), 0);

      event.getPlayer().swingMainHand();
      if(!event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
        InventoryUtil.removePartialStack(event.getPlayer(), event.getItemInHand(), 1);
    }

    private static boolean hasCooldown(UUID uuid) {
      long currentTick = NexoAddon.getInstance().getServer().getCurrentTick();
      return cooldowns.getOrDefault(uuid, 0L) > currentTick;
    }

    private static void setCooldown(UUID uuid, int ticks) {
      long currentTick = NexoAddon.getInstance().getServer().getCurrentTick();
      cooldowns.put(uuid, currentTick + ticks);
    }
  }
}
