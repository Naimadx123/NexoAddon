package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;

public record MiningTools(List<Material> materials, List<String> nexoIds, String type) {

  public static class MiningToolsListener implements Listener {
    @EventHandler
    public static void onBreak(NexoChorusBlockBreakEvent event){

      handle(event.getPlayer(), event.getMechanic(), event.getBlock().getLocation(), event);
    }

    @EventHandler
    public static void onBreak(NexoNoteBlockBreakEvent event){

      handle(event.getPlayer(), event.getMechanic(), event.getBlock().getLocation(), event);
    }

    private static void handle(Player player, CustomBlockMechanic mechanic, Location location, NexoBlockBreakEvent event){
      ItemStack tool = player.getInventory().getItemInMainHand();

      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(mechanic.getItemID());
      if (mechanics == null || mechanics.getMiningTools() == null) return;

      if(NexoItems.idFromItem(tool) != null
          && mechanics.getMiningTools().nexoIds().contains(NexoItems.idFromItem(tool))
      ) return;

      if(mechanics.getMiningTools().materials().contains(tool.getType())) return;

      event.setCancelled(true);

      if(mechanics.getMiningTools().type().equalsIgnoreCase("CANCEL_DROP")){

        List<Loot> loots = new ArrayList<>();
        Drop drop = new Drop(loots, false, false, NexoBlocks.customBlockMechanic(location).getItemID());
        if(ProtectionLib.canBreak(player, location)) {

          NexoBlocks.remove(location, null, drop);
          if(tool.getItemMeta() instanceof Damageable damageable){
            damageable.setDamage(damageable.getDamage() + 1);
            tool.setItemMeta(damageable);
          }
        }
      }
    }
  }
}
