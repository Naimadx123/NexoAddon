package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.lang.reflect.Method;

public record InventoryType(org.bukkit.event.inventory.InventoryType type, Component title) {

  public static class InventoryTypeListener implements Listener {

    @EventHandler
    public void on(NexoBlockInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK)
        return;
      handleInteract(event.getPlayer(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void on(NexoFurnitureInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND)
        return;
      handleInteract(event.getPlayer(), event.getMechanic().getItemID());
    }

    private void handleInteract(
        Player player,
        String nexoItemId
    ) {
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
      if (mechanics == null || mechanics.getInventoryType() == null)
        return;

      org.bukkit.event.inventory.InventoryType type = mechanics.getInventoryType().type();
      Component title = mechanics.getInventoryType().title();

      openVirtualInventory(player, type, title);
    }

    private void openVirtualInventory(
        Player p,
        org.bukkit.event.inventory.InventoryType type,
        Component title
    ) {
      switch (type) {
        case ENDER_CHEST -> {
          p.openInventory(p.getEnderChest());
        }
        case WORKBENCH -> {
          p.openWorkbench(p.getLocation(), true);
        }
        case CARTOGRAPHY -> {
          p.openCartographyTable(p.getLocation(), true);
        }
        case GRINDSTONE -> {
          p.openGrindstone(p.getLocation(), true);
        }
        case ANVIL -> {
          p.openAnvil(p.getLocation(), true);
        }
        case ENCHANTING -> {
          p.openEnchanting(p.getLocation(), true);
        }
        case SMITHING -> {
          p.openSmithingTable(p.getLocation(), true);
        }
        case STONECUTTER -> {
          p.openStonecutter(p.getLocation(), true);
        }
        default -> {
          Inventory inv = Bukkit.createInventory(null, type, title);
          p.openInventory(inv);
        }
      }
      setTitleIfSupported(p, title);
    }

    private void setTitleIfSupported(
        Player player,
        Component title
    ) {
      try {
        Object openInventory = player.getOpenInventory();

        Method setTitleMethod = openInventory.getClass().getMethod("setTitle", String.class);

        String serializedTitle = MiniMessage.miniMessage().serialize(title);
        setTitleMethod.invoke(openInventory, serializedTitle);
      } catch (NoSuchMethodException e) {
        // `setTitle` does not exist or is not supported in this version
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
