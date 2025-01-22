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
import zone.vao.nexoAddon.utils.VersionUtil;

import java.util.List;

public class EquippableListener {

  public static void onEquippable(final PlayerInteractEvent event) {
    Player player = event.getPlayer();

    if (!VersionUtil.isVersionLessThan("1.21.3")) return;

    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (isInteractingWithNexoBlockWhileHoldingHelmet(event, player)) {
        event.setCancelled(true);
        return;
      }

      String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
      if (itemId == null) return;

      if (!isValidInteraction(event)) return;

      if((event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND)) return;

      Components componentItem = NexoAddon.getInstance().getComponents().get(itemId);
      if (componentItem == null || componentItem.getEquippable() == null) return;

      equipItem(player, componentItem);
    }
  }

  private static boolean isInteractingWithNexoBlockWhileHoldingHelmet(PlayerInteractEvent event, Player player) {
    return event.getClickedBlock() != null
        && NexoBlocks.isNexoNoteBlock(event.getClickedBlock())
        && NexoItems.idFromItem(player.getInventory().getHelmet()) != null
        && getAllHelmets().contains(player.getInventory().getItemInMainHand().getType());
  }

  private static boolean isValidInteraction(PlayerInteractEvent event) {
    return event.getClickedBlock() == null || !NexoBlocks.isNexoNoteBlock(event.getClickedBlock());
  }

  private static void equipItem(Player player, Components componentItem) {
    ItemStack itemToEquip = player.getInventory().getItemInMainHand().clone();
    itemToEquip.setAmount(1);

    InventoryUtil.removePartialStack(player, player.getInventory().getItemInMainHand(), 1);

    ItemStack previousItem = player.getInventory().getItem(componentItem.getEquippable().getSlot());

    player.getInventory().setItem(componentItem.getEquippable().getSlot(), itemToEquip);

    returnPreviousItemToInventory(player, previousItem);
  }

  private static void returnPreviousItemToInventory(Player player, ItemStack previousItem) {
    if (previousItem != null && previousItem.getType() != Material.AIR) {
      new BukkitRunnable() {
        @Override
        public void run() {
          player.getInventory().addItem(previousItem);
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
