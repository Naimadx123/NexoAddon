package zone.vao.nexoAddon.events.playerInteracts;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Components;
import zone.vao.nexoAddon.utils.InventoryUtil;

import java.util.List;

public class EquippableListener {

  public static void onEquippable(final PlayerInteractEvent event) {
    Player player = event.getPlayer();

    if (!isValidInteraction(event, player)) return;

    String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
    if (itemId == null || !NexoAddon.getInstance().isComponentSupport()) return;

    Components componentItem = NexoAddon.getInstance().getComponents().get(itemId);
    if (componentItem == null || componentItem.getEquippable() == null) return;

    equipItem(event, player, componentItem);
  }

  private static boolean isValidInteraction(PlayerInteractEvent event, Player player) {
    return event.getHand() == EquipmentSlot.HAND
        && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        && (!NexoBlocks.isCustomBlock(event.getClickedBlock())
        || !isWearingCustomHelmet(player));
  }

  private static boolean isWearingCustomHelmet(Player player) {
    ItemStack helmet = player.getInventory().getHelmet();
    return helmet != null
        && NexoItems.idFromItem(helmet) != null
        && getAllHelmets().contains(player.getInventory().getItemInMainHand().getType());
  }

  private static void equipItem(PlayerInteractEvent event, Player player, Components componentItem) {
    event.setCancelled(true);

    ItemStack itemToEquip = player.getInventory().getItemInMainHand().clone();
    itemToEquip.setAmount(1);
    InventoryUtil.removePartialStack(player, 1);

    ItemStack existingHelmet = cloneHelmet(player.getInventory().getHelmet());

    equipToSlot(player, componentItem, itemToEquip);

    returnHelmetToInventory(player, existingHelmet);
  }

  private static ItemStack cloneHelmet(ItemStack helmet) {
    return (helmet != null && helmet.getType() != Material.AIR) ? helmet.clone() : null;
  }

  private static void equipToSlot(Player player, Components componentItem, ItemStack itemToEquip) {
    switch (componentItem.getEquippable().getSlot()) {
      case "CHESTPLATE" -> player.getInventory().setChestplate(itemToEquip);
      case "LEGGINGS" -> player.getInventory().setLeggings(itemToEquip);
      case "BOOTS" -> player.getInventory().setBoots(itemToEquip);
      default -> player.getInventory().setHelmet(itemToEquip);
    }
  }

  private static void returnHelmetToInventory(Player player, ItemStack existingHelmet) {
    if (existingHelmet != null) {
      new BukkitRunnable() {
        @Override
        public void run() {
          player.getInventory().addItem(existingHelmet);
        }
      }.runTaskLater(NexoAddon.getInstance(), 2);
    }
  }

  private static List<Material> getAllHelmets() {
    return List.of(
        Material.LEATHER_HELMET,
        Material.CHAINMAIL_HELMET,
        Material.IRON_HELMET,
        Material.GOLDEN_HELMET,
        Material.DIAMOND_HELMET,
        Material.NETHERITE_HELMET,
        Material.TURTLE_HELMET,
        Material.CARVED_PUMPKIN,
        Material.PLAYER_HEAD,
        Material.CREEPER_HEAD,
        Material.ZOMBIE_HEAD,
        Material.SKELETON_SKULL,
        Material.WITHER_SKELETON_SKULL,
        Material.DRAGON_HEAD
    );
  }
}
