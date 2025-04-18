package zone.vao.nexoAddon.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

  public static void removePartialStack(Player player, ItemStack mainHandItem, int amountToRemove) {

    if (mainHandItem.getType() == Material.AIR || mainHandItem.getAmount() < amountToRemove) {
      return;
    }

    mainHandItem.setAmount(mainHandItem.getAmount() - amountToRemove);

    if (mainHandItem.getAmount() <= 0) {
      player.getInventory().removeItem(mainHandItem);
    } else {
      player.getInventory().setItemInMainHand(mainHandItem);
    }
  }
}
