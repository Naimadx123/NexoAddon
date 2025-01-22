package zone.vao.nexoAddon.events.playerNexoBlockBreaks;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.ArrayList;
import java.util.List;

public class MiningToolsListener {

  public static void onBreak(NexoNoteBlockBreakEvent event){

    ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

    if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

    Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
    if (mechanics == null || mechanics.getMiningTools() == null) return;

    if(NexoItems.idFromItem(tool) != null
        && mechanics.getMiningTools().getNexoIds().contains(NexoItems.idFromItem(tool))
    ) return;

    if(mechanics.getMiningTools().getMaterials().contains(tool.getType())) return;

    event.setCancelled(true);

    if(mechanics.getMiningTools().getType().equalsIgnoreCase("CANCEL_DROP")){

      List<Loot> loots = new ArrayList<>();
      Drop drop = new Drop(loots, false, false, NexoBlocks.customBlockMechanic(event.getBlock().getLocation()).getItemID());
      if(ProtectionLib.canBreak(event.getPlayer(), event.getBlock().getLocation())) {

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getBlock(), event.getPlayer());
        if (!EventUtil.callEvent(blockBreakEvent)) return;

        NexoBlocks.remove(event.getBlock().getLocation(), null, drop);
        if(tool.getItemMeta() instanceof Damageable damageable){
          damageable.setDamage(damageable.getDamage() + 1);
          tool.setItemMeta(damageable);
        }
      }
    }
  }
}
