package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record AutoCatch(boolean toggable) {

    private static final NamespacedKey autoCatchKey = new NamespacedKey(NexoAddon.getInstance(), "auto_catch_enabled");

    public static boolean isAutoCatchTool(String toolId) {
        return toolId != null
            && NexoAddon.getInstance().getMechanics().containsKey(toolId)
            && NexoAddon.getInstance().getMechanics().get(toolId).getAutoCatch() != null;
    }

    public static boolean isAutoCatchEnabled(ItemStack item, boolean toggable) {
        if (item == null || item.getType() != Material.FISHING_ROD) return false;
        if (!toggable) return true;

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
        public static void onRodLeftClick(PlayerInteractEvent event) {
            if (
                (event.getAction() != Action.LEFT_CLICK_BLOCK)
                || event.getHand() == null
                || !event.getHand().equals(EquipmentSlot.HAND)
            ) return;

            Player player = event.getPlayer();

            String toolId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
            if(NexoAddon.getInstance().getMechanics().get(toolId).getAutoCatch() == null || !NexoAddon.getInstance().getMechanics().get(toolId).getAutoCatch().toggable()) return;

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.FISHING_ROD) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            PersistentDataContainer container = meta.getPersistentDataContainer();

            boolean current = container.getOrDefault(autoCatchKey, PersistentDataType.BOOLEAN, false);
            container.set(autoCatchKey, PersistentDataType.BOOLEAN, !current);
            item.setItemMeta(meta);

            player.sendMessage(MiniMessage.miniMessage().deserialize("<grey>AutoCatch is now " + (!current ? "<green>enabled" : "<red>disabled") + "<grey>."));
            player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
        }

        @EventHandler
        public static void onAutoCatch(PlayerFishEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getInventory().getItemInMainHand();
            String toolId = NexoItems.idFromItem(tool);
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(toolId);

            if (!AutoCatch.isAutoCatchTool(toolId) || !AutoCatch.isAutoCatchEnabled(tool, mechanics.getAutoCatch().toggable())) return;

            if (event.getState() == PlayerFishEvent.State.BITE || event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                int delay = event.getState() == PlayerFishEvent.State.BITE ? 10 : 20;
                NexoAddon.getInstance().getFoliaLib().getScheduler().runLater(() -> {
                    player.swingHand(EquipmentSlot.HAND);
                    Object hand = getFishingRodHand(player);
                    if (hand != null) {
                        simulateRodCast(player, hand);
                    }
                }, delay);
            }
        }

        private static void simulateRodCast(Player player, Object hand) {
            Object serverPlayer;
            try {
                serverPlayer = player.getClass().getMethod("getHandle").invoke(player);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                ex.printStackTrace();
                return;
            }

            Class<?> handClass = hand.getClass();
            try {
                Method getItemInHand = serverPlayer.getClass().getMethod("getItemInHand", handClass);
                Object nmsItemStack = getItemInHand.invoke(serverPlayer, hand);

                Object level;
                try {
                    level = serverPlayer.getClass().getMethod("level").invoke(serverPlayer);
                } catch (NoSuchMethodException e) {
                    try {
                        level = serverPlayer.getClass().getMethod("getLevel").invoke(serverPlayer);
                    } catch (NoSuchMethodException ex) {
                        Field levelField = serverPlayer.getClass().getField("level");
                        level = levelField.get(serverPlayer);
                    }
                }

                Field gameModeField = serverPlayer.getClass().getField("gameMode");
                Object gameMode = gameModeField.get(serverPlayer);

                Method useItem = null;
                for (Method m : gameMode.getClass().getMethods()) {
                    if (m.getName().equals("useItem") && m.getParameterCount() == 4) {
                        Class<?>[] params = m.getParameterTypes();
                        if (params[0].isAssignableFrom(serverPlayer.getClass()) && params[3].equals(handClass)) {
                            useItem = m;
                            break;
                        }
                    }
                }
                if (useItem != null) {
                    useItem.invoke(gameMode, serverPlayer, level, nmsItemStack, hand);
                }

                Method swing = null;
                try {
                    swing = serverPlayer.getClass().getMethod("swing", handClass);
                    swing.invoke(serverPlayer, hand);
                } catch (NoSuchMethodException ex) {
                    swing = serverPlayer.getClass().getMethod("swing", handClass, boolean.class);
                    swing.invoke(serverPlayer, hand, true);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private static Object getFishingRodHand(Player player) {
            try {
                Class<?> handClass = getHandClass();
                Object[] hands = handClass.getEnumConstants();
                Object mainHand = hands.length > 0 ? hands[0] : null;
                Object offHand = hands.length > 1 ? hands[1] : null;

                if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
                    return mainHand;
                }
                if (player.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD) {
                    return offHand;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        private static Class<?> getHandClass() throws ClassNotFoundException {
            try {
                return Class.forName("net.minecraft.world.InteractionHand");
            } catch (ClassNotFoundException ex) {
                try {
                    return Class.forName("net.minecraft.world.EnumHand");
                } catch (ClassNotFoundException ex2) {
                    return Class.forName("net.minecraft.util.Hand");
                }
            }
        }
    }
}
