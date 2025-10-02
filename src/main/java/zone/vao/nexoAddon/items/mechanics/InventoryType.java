package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public record InventoryType(org.bukkit.event.inventory.InventoryType type, Component title) {

  public static class InventoryTypeListener implements Listener {
    @EventHandler
    public void on(NexoBlockInteractEvent event) {

      if(event.getHand() != EquipmentSlot.HAND && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

      handleInteract(event.getPlayer());
    }

    @EventHandler
    public void on(NexoFurnitureInteractEvent event) {
      if(event.getHand() != EquipmentSlot.HAND) return;

      handleInteract(event.getPlayer());
    }

    private void handleInteract(Player p){

    }
  }
}
