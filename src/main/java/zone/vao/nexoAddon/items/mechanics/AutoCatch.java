package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;

import java.lang.reflect.InvocationTargetException;

public record AutoCatch(boolean toggable) {

    private static final NamespacedKey autoCatchKey = new NamespacedKey(NexoAddon.getInstance(), "auto_catch_enabled");

    public static boolean isAutoCatchTool(String toolId) {
        return toolId != null
                && NexoAddon.getInstance().getMechanics().containsKey(toolId)
                && NexoAddon.getInstance().getMechanics().get(toolId).getBigMining() != null;
    }

    public static boolean isAutoCatchEnabled(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().getOrDefault(
                autoCatchKey,
                PersistentDataType.BOOLEAN,
                false
        );
    }

    public static class AutoCatchListener implements Listener {

        @EventHandler
        public void onRodLeftClick(PlayerInteractEvent event) {
            if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() != Material.FISHING_ROD) return;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer container = meta.getPersistentDataContainer();

            boolean current = container.getOrDefault(autoCatchKey, PersistentDataType.BOOLEAN, false);
            container.set(autoCatchKey, PersistentDataType.BOOLEAN, !current);
            item.setItemMeta(meta);

            player.sendMessage("§7AutoCatch is now §b" + (!current ? "enabled" : "disabled") + "§7.");
            player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
        }

        @EventHandler
        public void onAutoCatch(PlayerFishEvent event) {
            if (event.isCancelled()) return;

            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();

            String toolId = NexoItems.idFromItem(tool);
            if (!AutoCatch.isAutoCatchTool(toolId) || !AutoCatch.isAutoCatchEnabled(tool)) return;

            if (event.getState() == PlayerFishEvent.State.BITE || event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                int delay = event.getState() == PlayerFishEvent.State.BITE ? 10 : 20;

                NexoAddon.getInstance().getFoliaLib().getScheduler().runLater(() -> {
                    InteractionHand hand = getFishingRodHand(player);
                    if (hand == null) return;
                    simulateRodCast(player, hand);
                }, delay);
            }
        }

        private static void simulateRodCast(Player player, InteractionHand hand) {
            ServerPlayer serverPlayer;
            try {
                serverPlayer = (ServerPlayer) player.getClass().getMethod("getHandle").invoke(player);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }

            serverPlayer.gameMode.useItem(serverPlayer, serverPlayer.level(), serverPlayer.getItemInHand(hand), hand);
            serverPlayer.swing(hand, true);
        }

        private static InteractionHand getFishingRodHand(Player player) {
            if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
                return InteractionHand.MAIN_HAND;
            } else if (player.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD) {
                return InteractionHand.OFF_HAND;
            } else {
                return null;
            }
        }
    }
}
