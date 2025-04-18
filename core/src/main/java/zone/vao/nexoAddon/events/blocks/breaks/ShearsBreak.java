package zone.vao.nexoAddon.events.blocks.breaks;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.handlers.TallStringBlocksHandler;

public class ShearsBreak {

  public static void onBlockBreak(BlockBreakEvent event) {

    handleStringBlocks(event);
    handleChorusBlocks(event);
  }

  private static void handleStringBlocks(BlockBreakEvent event){
    if(!TallStringBlocksHandler.isStringBlock(event.getBlock())
        || !event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHEARS)
        || !NexoAddon.getInstance().getGlobalConfig().getStringList("count_shears_as_silktouch").contains(TallStringBlocksHandler.getStringBlockId(event.getBlock()))
        || !ProtectionLib.canBreak(event.getPlayer(), event.getBlock().getLocation())
    ) {
      return;
    }

    event.setCancelled(true);
    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
        NexoItems.itemFromId(TallStringBlocksHandler.getStringBlockId(event.getBlock())).build());

    TallStringBlocksHandler.removeStringBlock(event.getBlock(), false);
  }

  private static void handleChorusBlocks(BlockBreakEvent event){
    if(!NexoBlocks.isNexoChorusBlock(event.getBlock())
        || !event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHEARS)
        || !NexoAddon.getInstance().getGlobalConfig().getStringList("count_shears_as_silktouch").contains(NexoBlocks.chorusBlockMechanic(event.getBlock()).getItemID())
        || !ProtectionLib.canBreak(event.getPlayer(), event.getBlock().getLocation())
    ) {
      return;
    }

    event.setCancelled(true);
    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
        NexoItems.itemFromId(NexoBlocks.chorusBlockMechanic(event.getBlock()).getItemID()).build());

    event.getBlock().setType(Material.AIR);
  }
}
