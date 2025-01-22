package zone.vao.nexoAddon.events.inventoryClicks;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;

public class RepairListener {

  public static void onInventoryClick(InventoryClickEvent event) {
    if (!isValidClick(event)) return;

    Player player = (Player) event.getWhoClicked();
    ItemStack cursorItem = event.getCursor().clone();
    ItemStack currentItem = event.getCurrentItem().clone();

    String repairItemId = NexoItems.idFromItem(cursorItem);
    if (!canRepair(repairItemId, currentItem)) return;

    event.setCancelled(true);

    repairItem(player, cursorItem, currentItem, repairItemId);
    updatePlayerInventory(player, currentItem, cursorItem, event);
  }

  private static boolean isValidClick(InventoryClickEvent event) {
    return event.getWhoClicked() instanceof Player &&
        event.isLeftClick() &&
        event.getCursor() != null &&
        event.getCurrentItem() != null;
  }

  private static boolean canRepair(String repairItemId, ItemStack currentItem) {
    if (repairItemId == null) return false;

    if (!(currentItem.getItemMeta() instanceof Damageable itemMeta) || !itemMeta.hasDamage()) return false;

    Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(repairItemId);
    if (mechanics == null || mechanics.getRepair() == null) return false;

    String currentItemId = NexoItems.idFromItem(currentItem);
    if (currentItemId != null) {
      Mechanics currentMechanics = NexoAddon.getInstance().getMechanics().get(currentItemId);
      if (currentMechanics != null && currentMechanics.getRepair() != null) return false;
    }

    return true;
  }

  private static void repairItem(Player player, ItemStack cursorItem, ItemStack currentItem, String repairItemId) {
    Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(repairItemId);
    double repairRatio = mechanic.getRepair().getRatio();
    int fixedAmount = mechanic.getRepair().getFixedAmount();
    int maxDurability = NexoItems.itemFromId(repairItemId).getDurability() != null ? NexoItems.itemFromId(repairItemId).getDurability() : NexoItems.itemFromId(repairItemId).build().getType().getMaxDurability();
    if(repairRatio > 0) {

      Damageable currentMeta = (Damageable) currentItem.getItemMeta();
      int repairAmount = (int) (currentMeta.getDamage() * repairRatio);
      currentMeta.setDamage(Math.max(0, currentMeta.getDamage() - repairAmount));
      currentItem.setItemMeta(currentMeta);

      if (maxDurability > 0) {
        updateCursorItemWithDurability(cursorItem, maxDurability);
      } else {
        reduceCursorItemAmount(cursorItem);
      }
    }else if(fixedAmount > 0){

      Damageable currentMeta = (Damageable) currentItem.getItemMeta();
      currentMeta.setDamage(Math.max(0, currentMeta.getDamage() - fixedAmount));
      currentItem.setItemMeta(currentMeta);

      if (maxDurability > 0) {
        updateCursorItemWithDurability(cursorItem, maxDurability);
      } else {
        reduceCursorItemAmount(cursorItem);
      }
    }
  }

  private static void updateCursorItemWithDurability(ItemStack cursorItem, int maxDurability) {
    Damageable cursorMeta = (Damageable) cursorItem.getItemMeta();
    int currentDamage = cursorMeta.getDamage();
    int newDamage = currentDamage + 1;

    if (newDamage >= maxDurability) {
      cursorItem.setAmount(cursorItem.getAmount() - 1);
      if (cursorItem.getAmount() <= 0) {
        cursorItem.setType(Material.AIR);
      } else {
        cursorMeta.setDamage(0);
        cursorItem.setItemMeta(cursorMeta);
      }
    } else {
      cursorMeta.setDamage(newDamage);
      cursorItem.setItemMeta(cursorMeta);
    }
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
}
