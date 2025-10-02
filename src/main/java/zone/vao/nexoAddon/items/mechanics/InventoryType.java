package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

public record InventoryType(org.bukkit.event.inventory.InventoryType type, Component title) {

  public static class InventoryTypeListener implements Listener {

    @EventHandler
    public void on(NexoBlockInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
      handleInteract(event.getPlayer(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void on(NexoFurnitureInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND) return;
      handleInteract(event.getPlayer(), event.getMechanic().getItemID());
    }

    private void handleInteract(Player player, String nexoItemId) {
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
      if (mechanics == null || mechanics.getInventoryType() == null) return;

      org.bukkit.event.inventory.InventoryType type = mechanics.getInventoryType().type();
      Component title = mechanics.getInventoryType().title();

      openVirtualInventory(player, type, title);
    }

    private void openVirtualInventory(Player p, org.bukkit.event.inventory.InventoryType type, Component title) {
      switch (type) {
        case ENDER_CHEST -> {
          p.openInventory(p.getEnderChest());
        }
        case PLAYER -> {
        }
        default -> {
          Inventory inv = Bukkit.createInventory(null, type, title);
          p.openInventory(inv);
        }
      }
    }
  }
}
