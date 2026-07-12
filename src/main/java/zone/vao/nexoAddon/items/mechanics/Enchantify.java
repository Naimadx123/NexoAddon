package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.List;
import java.util.Map;

public record Enchantify(Map<Enchantment, Integer> enchants, Map<Enchantment, Integer> limits, List<Material> materials, List<String> nexoIds, List<Material> materialsBlacklist, List<String> nexoIdsBlacklist) {

  public static class EnchantifyListener implements Listener {

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
      if (!isValidClick(event)) return;

      Player player = (Player) event.getWhoClicked();
      ItemStack cursorItem = event.getCursor().clone();
      ItemStack currentItem = event.getCurrentItem().clone();

      String enchantifyItemId = NexoItems.idFromItem(cursorItem);
      if (!canEnchantify(enchantifyItemId, currentItem)) return;

      event.setCancelled(true);

      boolean enchanted = enchantItem(player, cursorItem, currentItem, enchantifyItemId);
      if (!enchanted) return;

      updatePlayerInventory(player, currentItem, cursorItem, event);
    }

    private static boolean enchantItem(Player player, ItemStack cursorItem, ItemStack currentItem, String enchantifyItemId) {
      Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(enchantifyItemId);
      Map<Enchantment, Integer> enchants = mechanic.getEnchantify().enchants();
      Map<Enchantment, Integer> limits = mechanic.getEnchantify().limits();

      ItemMeta meta = currentItem.getItemMeta();
      boolean changed = false;
      for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
        Enchantment enchant = entry.getKey();
        int addLevel = entry.getValue();
        int existingLevel = meta.getEnchantLevel(enchant);
        int newLevel = existingLevel + addLevel;

        if (limits.containsKey(enchant)) {
          newLevel = Math.min(newLevel, limits.get(enchant));
        }

        if (newLevel > existingLevel) {
          meta.addEnchant(enchant, newLevel, true);
          changed = true;
        }
      }

      if (!changed) return false;

      currentItem.setItemMeta(meta);

      reduceCursorItemAmount(cursorItem);
      return true;
    }

    private static boolean isValidClick(InventoryClickEvent event) {
      return event.getWhoClicked() instanceof Player &&
          event.isLeftClick() &&
          event.getCursor() != null &&
          event.getCurrentItem() != null;
    }

    private static boolean canEnchantify(String enchantifyItemId, ItemStack currentItem) {
      if (enchantifyItemId == null) return false;

      if (currentItem.getType() == Material.AIR) return false;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(enchantifyItemId);

      if (mechanics == null || mechanics.getEnchantify() == null) return false;

      Enchantify enchantify = mechanics.getEnchantify();
      if (!isItemAllowed(currentItem, enchantify)) return false;

      String currentItemId = NexoItems.idFromItem(currentItem);
      if (currentItemId != null) {
        Mechanics currentMechanics = NexoAddon.getInstance().getMechanics().get(currentItemId);
        return currentMechanics == null || currentMechanics.getEnchantify() == null;
      }

      return true;
    }

    private static void reduceCursorItemAmount(ItemStack cursorItem) {
      if (cursorItem.getAmount() > 1) {
        cursorItem.setAmount(cursorItem.getAmount() - 1);
      } else {
        cursorItem.setType(Material.AIR);
      }
    }

    private static void updatePlayerInventory(Player player, ItemStack currentItem, ItemStack cursorItem, InventoryClickEvent event) {
      event.getClickedInventory().setItem(event.getSlot(), currentItem);
      player.setItemOnCursor(cursorItem == null ? new ItemStack(Material.AIR) : cursorItem);
      player.updateInventory();
    }

    private static boolean isItemAllowed(ItemStack item, Enchantify enchantify) {
      if (item == null || item.getType() == Material.AIR) {
        return false;
      }
      boolean whitelistDefined = !enchantify.materials().isEmpty() || !enchantify.nexoIds().isEmpty();
      boolean blacklistDefined = !enchantify.materialsBlacklist().isEmpty() || !enchantify.nexoIdsBlacklist().isEmpty();
      if (blacklistDefined) {
        if (enchantify.materialsBlacklist().contains(item.getType())) {
          return false;
        }
        String itemId = NexoItems.idFromItem(item);
        if (itemId != null && enchantify.nexoIdsBlacklist().contains(itemId)) {
          return false;
        }
      }
      if (whitelistDefined) {
        boolean whitelisted = enchantify.materials().contains(item.getType());
        if (!whitelisted) {
          String itemId = NexoItems.idFromItem(item);
          whitelisted = itemId != null && enchantify.nexoIds().contains(itemId);
        }
        return whitelisted;
      }
      return true;
    }
  }
}
