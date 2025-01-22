package zone.vao.nexoAddon.events.blockBreaks;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.handlers.TallStringBlocksHandler;


public class ShearsBreak {

  public static void onBlockBreak(BlockBreakEvent event) {

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
}
