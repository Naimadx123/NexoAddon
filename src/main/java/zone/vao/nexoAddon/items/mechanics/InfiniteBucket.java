package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;

public record InfiniteBucket(boolean enabled, int uses) {

    public static final NamespacedKey USES_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "infinite_bucket_uses");

    private static final NamespacedKey LORE_INDEX_KEY =
        new NamespacedKey(NexoAddon.getInstance(), "infinite_bucket_lore_index");

    public static void initUses(ItemStack item, int maxUses) {
        if (maxUses < 0) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, maxUses);
        applyLore(meta, maxUses);
        item.setItemMeta(meta);
    }

    private static void applyLore(ItemMeta meta, int remaining) {
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Component usesLine = MiniMessage.miniMessage()
            .deserialize("<!italic><gray>Verwendungen: <white>" + remaining + "</white></gray>");

        if (pdc.has(LORE_INDEX_KEY, PersistentDataType.INTEGER)) {
            Integer idxBoxed = pdc.get(LORE_INDEX_KEY, PersistentDataType.INTEGER);
            int idx = idxBoxed != null ? idxBoxed : -1;
            if (idx >= 0 && idx < lore.size()) {
                lore.set(idx, usesLine);
            } else {
                lore.add(usesLine);
                pdc.set(LORE_INDEX_KEY, PersistentDataType.INTEGER, lore.size() - 1);
            }
        } else {
            lore.add(usesLine);
            pdc.set(LORE_INDEX_KEY, PersistentDataType.INTEGER, lore.size() - 1);
        }

        meta.lore(lore);
    }

    public static class InfiniteBucketListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onBucketEmpty(PlayerBucketEmptyEvent event) {
            Player player = event.getPlayer();
            ItemStack bucket = player.getInventory().getItemInMainHand();

            String nexoItemId = NexoItems.idFromItem(bucket);
            if (nexoItemId == null) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getInfiniteBucket() == null) {
                return;
            }

            InfiniteBucket infiniteBucket = mechanics.getInfiniteBucket();
            if (!infiniteBucket.enabled()) {
                return;
            }

            if (!ProtectionLib.canBuild(player, event.getBlock().getLocation())) {
                return;
            }

            Material liquid;
            if (event.getBucket() == Material.WATER_BUCKET) {
                liquid = Material.WATER;
            } else if (event.getBucket() == Material.LAVA_BUCKET) {
                liquid = Material.LAVA;
            } else {
                return;
            }

            if (infiniteBucket.uses() >= 0) {
                ItemMeta meta = bucket.getItemMeta();
                if (meta == null) {
                    return;
                }

                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                int remaining = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, infiniteBucket.uses());

                if (remaining <= 0) {
                    return;
                }

                int newRemaining = remaining - 1;
                pdc.set(USES_KEY, PersistentDataType.INTEGER, newRemaining);
                applyLore(meta, newRemaining);
                bucket.setItemMeta(meta);
            }

            player.getWorld().getBlockAt(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())
                .setType(liquid);
            event.setCancelled(true);
            player.getInventory().setItemInMainHand(bucket);
        }
    }
}


