package zone.vao.nexoAddon.utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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

  public static void damageItem(Player player, ItemStack tool, int damage) {
    if (tool == null || tool.getType() == Material.AIR) return;

    int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);

    ItemMeta meta = tool.getItemMeta();
    if (!(meta instanceof Damageable damageable)) return;

    int finalDamage = 0;
    for (int i = 0; i < damage; i++) {
      if (Math.random() < 1.0 / (unbreakingLevel + 1)) {
        finalDamage++;
      }
    }

    int newDamage = damageable.getDamage() + finalDamage;
    int maxDurability = tool.getType().getMaxDurability();

    if (newDamage >= maxDurability) {
      player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
    } else {
      damageable.setDamage(newDamage);
      tool.setItemMeta(meta);
    }
  }
}
