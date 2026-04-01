package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;

import java.util.Set;
import java.util.logging.Logger;

import static zone.vao.nexoAddon.utils.BlockUtil.isInteractable;

public record WideHoe(int radius, boolean switchable, boolean tillGrass, int durabilityCost) {

    private static final Set<Material> TILLABLE = Set.of(
        Material.DIRT,
        Material.COARSE_DIRT,
        Material.ROOTED_DIRT,
        Material.DIRT_PATH
    );

    private static final Set<Material> TILLABLE_GRASS = Set.of(
        Material.GRASS_BLOCK,
        Material.MYCELIUM,
        Material.PODZOL
    );

    public static boolean isWideHoeTool(String toolId) {
        return toolId != null
               && NexoAddon.getInstance().getMechanics().containsKey(toolId)
               && NexoAddon.getInstance().getMechanics().get(toolId).getWideHoe() != null;
    }

    boolean canTill(Material mat) {
        return TILLABLE.contains(mat) || (tillGrass && TILLABLE_GRASS.contains(mat));
    }

    // -----------------------------------------------------------------------

    public static class WideHoeListener implements Listener {

        private static final Logger log = NexoAddon.getInstance().getLogger();

        private static final NamespacedKey KEY =
            new NamespacedKey(NexoAddon.getInstance(), "wideHoeSwitchable");

        // ── Tilling ──────────────────────────────────────────────────────────

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onHoeUse(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            String toolId = NexoItems.idFromItem(tool);

            log.info("[WideHoe] RIGHT_CLICK_BLOCK | player=" + player.getName()
                + " | toolId=" + toolId
                + " | isWideHoeTool=" + WideHoe.isWideHoeTool(toolId));

            if (!WideHoe.isWideHoeTool(toolId)) {
                return;
            }

            Block clicked = event.getClickedBlock();
            if (clicked == null) {
                return;
            }

            WideHoe mechanic = NexoAddon.getInstance().getMechanics().get(toolId).getWideHoe();

            // Check toggle state – same pattern as BigMining:
            // no PDC entry yet = mechanic is ON (never toggled off)
            if (mechanic.switchable() && tool.getItemMeta() != null) {
                PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
                if (pdc.has(KEY, PersistentDataType.BOOLEAN)
                    && Boolean.FALSE.equals(pdc.get(KEY, PersistentDataType.BOOLEAN))) {
                    log.info("[WideHoe] " + player.getName() + " has WideHoe disabled, skipping.");
                    return;
                }
            }

            if (!mechanic.canTill(clicked.getType())) {
                log.info("[WideHoe] Block " + clicked.getType() + " is not tillable, skipping.");
                return;
            }

            log.info("[WideHoe] " + player.getName() + " tilling " + mechanic.radius() + "x" + mechanic.radius()
                     + " around " + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ());

            // Cancel vanilla so the server doesn't do a single-block till on its own
            event.setCancelled(true);

            // Run on next tick – stays on the main thread (no async race conditions)
            NexoAddon.getInstance().getFoliaLib().getScheduler()
                .runNextTick(t -> tillRadius(player, tool, clicked, mechanic));
        }

        private void tillRadius(Player player, ItemStack tool, Block origin, WideHoe mechanic) {
            int half = mechanic.radius() / 2;
            int tilled = 0;

            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    Block target = origin.getRelative(dx, 0, dz);

                    if (!mechanic.canTill(target.getType())) {
                        continue;
                    }

                    // Don't till under solid blocks (vanilla behaviour)
                    Block above = target.getRelative(BlockFace.UP);
                    if (!above.getType().isAir() && above.getType().isSolid()) {
                        continue;
                    }

                    if (!ProtectionLib.canBuild(player, target.getLocation())) {
                        continue;
                    }

                    target.setType(Material.FARMLAND);
                    tilled++;

                    if (mechanic.durabilityCost() > 0) {
                        if (!applyDurability(player, tool, mechanic.durabilityCost())) {
                            log.info("[WideHoe] Tool broke for " + player.getName() + " after " + tilled + " blocks.");
                            return;
                        }
                    }
                }
            }

            log.info("[WideHoe] Tilled " + tilled + " blocks for " + player.getName());
            origin.getWorld().playSound(origin.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
        }

        /**
         * Applies durability. Returns false if the tool broke.
         */
        private boolean applyDurability(Player player, ItemStack tool, int amount) {
            if (!(tool.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable)) {
                return true;
            }

            int newDamage = damageable.getDamage() + amount;
            if (newDamage >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                return false;
            }
            damageable.setDamage(newDamage);
            tool.setItemMeta(damageable);
            return true;
        }

        // ── Toggle ── (identical pattern to BigMining) ───────────────────────

        @EventHandler(ignoreCancelled = true)
        public void onToggle(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            String toolId = NexoItems.idFromItem(tool);

            if (!WideHoe.isWideHoeTool(toolId) || event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            if (event.getClickedBlock() != null && isInteractable(event.getClickedBlock())) {
                return;
            }

            // If the player right-clicked a tillable block, that's handled by onHoeUse – not a toggle
            if (event.getClickedBlock() != null) {
                WideHoe mechanic = NexoAddon.getInstance().getMechanics().get(toolId).getWideHoe();
                if (mechanic.canTill(event.getClickedBlock().getType())) {
                    return;
                }
            }

            WideHoe mechanic = NexoAddon.getInstance().getMechanics().get(toolId).getWideHoe();
            if (!mechanic.switchable() || tool.getItemMeta() == null) {
                return;
            }

            var meta = tool.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            // First time: set to true (ON) – exactly like BigMining
            if (!pdc.has(KEY, PersistentDataType.BOOLEAN)) {
                pdc.set(KEY, PersistentDataType.BOOLEAN, true);
                tool.setItemMeta(meta);
                turnOn(player, pdc);
                log.info("[WideHoe] " + player.getName() + " toggled WideHoe ON (first use).");
                return;
            }

            boolean isOn = Boolean.TRUE.equals(pdc.get(KEY, PersistentDataType.BOOLEAN));
            if (isOn) {
                turnOff(player, pdc);
                log.info("[WideHoe] " + player.getName() + " toggled WideHoe OFF.");
            } else {
                turnOn(player, pdc);
                log.info("[WideHoe] " + player.getName() + " toggled WideHoe ON.");
            }
            tool.setItemMeta(meta);
        }

        private static void turnOn(Player player, PersistentDataContainer pdc) {
            pdc.set(KEY, PersistentDataType.BOOLEAN, true);
            Audience.audience(player).sendActionBar(
                MiniMessage.miniMessage().deserialize(
                    NexoAddon.getInstance().getGlobalConfig()
                        .getString("messages.widehoe.enabled", "<green>WideHoe on")));
        }

        private static void turnOff(Player player, PersistentDataContainer pdc) {
            pdc.set(KEY, PersistentDataType.BOOLEAN, false);
            Audience.audience(player).sendActionBar(
                MiniMessage.miniMessage().deserialize(
                    NexoAddon.getInstance().getGlobalConfig()
                        .getString("messages.widehoe.disabled", "<red>WideHoe off")));
        }
    }
}
