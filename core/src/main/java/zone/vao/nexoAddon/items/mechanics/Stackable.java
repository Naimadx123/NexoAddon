package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.utils.blocksounds.BlockSounds;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.GameMode;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

public record Stackable(String next, String group) {

  public static class StackableListener implements Listener {

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
  }
}
