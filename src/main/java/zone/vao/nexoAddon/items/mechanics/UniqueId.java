package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.UUID;

public record UniqueId(boolean enabled) {

    static NamespacedKey key = new NamespacedKey(NexoAddon.getInstance(), "unique_id");

    public static boolean isUniqueIdTool(String toolId) {
        if (toolId == null) return false;
        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(toolId);
        return mechanics != null && mechanics.getUniqueId() != null && mechanics.getUniqueId().enabled();
    }

    public static class UniqueIdListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onSlotChange(PlayerInventorySlotChangeEvent event) {
            ItemStack newItem = event.getNewItemStack();
            if (newItem.getType().isAir()) return;
            if (tagIfMissing(newItem)) {
                event.getPlayer().getInventory().setItem(event.getSlot(), newItem);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPickup(EntityPickupItemEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            ItemStack stack = event.getItem().getItemStack();
            tagIfMissing(stack);
        }

        @EventHandler(ignoreCancelled = true)
        public void onClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            NexoAddon.getInstance().getFoliaLib().getScheduler().runNextTick((tag) -> tagAllInInventory(player));
        }

        @EventHandler(ignoreCancelled = true)
        public void onDrag(InventoryDragEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            NexoAddon.getInstance().getFoliaLib().getScheduler().runNextTick((tag) -> tagAllInInventory(player));
        }

        private boolean tagIfMissing(ItemStack item) {
            if (item == null || item.getType().isAir()) return false;
            if (!isUniqueIdTool(NexoItems.idFromItem(item))) return false;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(UniqueId.key, PersistentDataType.STRING)) return false;
            pdc.set(UniqueId.key, PersistentDataType.STRING, UUID.randomUUID().toString());
            item.setItemMeta(meta);
            return true;
        }

        private void tagAllInInventory(Player player) {
            PlayerInventory inv = player.getInventory();
            for (ItemStack it : inv.getContents()) tagIfMissing(it);
            for (ItemStack it : inv.getStorageContents()) tagIfMissing(it);
            for (ItemStack it : inv.getArmorContents()) tagIfMissing(it);
            for (ItemStack it : inv.getExtraContents()) tagIfMissing(it);
        }
    }
}
