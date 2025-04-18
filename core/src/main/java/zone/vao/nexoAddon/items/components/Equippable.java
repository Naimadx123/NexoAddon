package zone.vao.nexoAddon.items.components;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.utils.InventoryUtil;
import zone.vao.nexoAddon.utils.VersionUtil;

import java.util.List;

public record Equippable(EquipmentSlot slot) {

  public static class EquippableListener implements Listener {

    @EventHandler
    public static void onInventoryClick(InventoryClickEvent event) {
      if (!VersionUtil.isVersionLessThan("1.21.3")) return;

      if(!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null) return;

      if(event.getCursor().isEmpty() || NexoItems.idFromItem(event.getCursor()) == null) return;

      Components components = NexoAddon.getInstance().getComponents().get(NexoItems.idFromItem(event.getCursor()));
      if (components == null || components.getEquippable() == null) return;

      if(!event.getSlotType().equals(InventoryType.SlotType.ARMOR)) return;

      int slot = event.getSlot();
      int equippedSlot = 0;
      switch (components.getEquippable().slot()) {
        case HEAD -> equippedSlot = 39;
        case CHEST -> equippedSlot = 38;
        case LEGS -> equippedSlot = 37;
        case FEET -> equippedSlot = 36;
      }

      if(slot != equippedSlot) return;

      ItemStack currItem = event.getCurrentItem();
      ItemStack cursorItem = event.getCursor();
      if(currItem != null)
        currItem = currItem.clone();

      ItemStack finalCurrItem = currItem;
      new BukkitRunnable() {
        @Override
        public void run() {
          player.getInventory().setItem(components.getEquippable().slot(), cursorItem);
          player.setItemOnCursor(finalCurrItem);
        }
      }.runTaskLater(NexoAddon.getInstance(), 1L);
    }

    @EventHandler
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

      ItemStack previousItem = player.getInventory().getItem(componentItem.getEquippable().slot());

      player.getInventory().setItem(componentItem.getEquippable().slot(), itemToEquip);

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
}
