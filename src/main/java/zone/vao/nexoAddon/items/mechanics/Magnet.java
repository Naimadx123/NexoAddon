package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

public record Magnet(boolean enabled, int radius, double pullSpeed,
                     String particleType, int particleAmount,
                     String soundType, float soundVolume, float soundPitch,
                     String activeLore, String inactiveLore) {

    private static final NamespacedKey ACTIVE_KEY = new NamespacedKey(NexoAddon.getInstance(), "magnet_active");
    private static final MiniMessage MINI = MiniMessage.builder()
        .postProcessor(pro -> pro.decoration(TextDecoration.ITALIC, false)).build();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static boolean hasMagnet(String itemId) {
        if (itemId == null) {
            return false;
        }
        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
        return mechanics != null && mechanics.getMagnet() != null && mechanics.getMagnet().enabled();
    }

    /**
     * Gibt zurück ob der Magnet für dieses Item-Stack aktiv ist (Standard: true).
     */
    public static boolean isActive(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return true;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(ACTIVE_KEY, PersistentDataType.BOOLEAN)) {
            return true;
        }
        return Boolean.TRUE.equals(pdc.get(ACTIVE_KEY, PersistentDataType.BOOLEAN));
    }

    /**
     * Setzt den Aktiv-Status und aktualisiert die Lore des Items.
     */
    public static void setActive(ItemStack item, boolean active, Magnet magnet) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.getPersistentDataContainer().set(ACTIVE_KEY, PersistentDataType.BOOLEAN, active);

        Component newLine = MINI.deserialize(active ? magnet.activeLore() : magnet.inactiveLore());
        String newLinePlain = PLAIN.serialize(newLine);

        // Lore-Zeile des anderen Zustands zum Suchen
        Component oldLine = MINI.deserialize(active ? magnet.inactiveLore() : magnet.activeLore());
        String oldLinePlain = PLAIN.serialize(oldLine);

        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String linePlain = PLAIN.serialize(lore.get(i));
            if (linePlain.equals(oldLinePlain) || linePlain.equals(newLinePlain)) {
                lore.set(i, newLine);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lore.add(newLine);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static class MagnetListener implements Listener {

        @EventHandler
        public void onRightClick(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND) {
                return;
            }
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            String itemId = NexoItems.idFromItem(item);

            if (!hasMagnet(itemId)) {
                return;
            }

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
            Magnet magnet = mechanics.getMagnet();

            boolean nowActive = !isActive(item);
            setActive(item, nowActive, magnet);
            // ItemStack-Referenz im Inventar aktualisieren
            player.getInventory().setItemInMainHand(item);
        }

        public static void startMagnetTask() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : NexoAddon.getInstance().getServer().getOnlinePlayers()) {
                        if (player.isDead()) {
                            continue;
                        }

                        // Ganzes Inventar nach aktivem Magnet-Item durchsuchen
                        for (ItemStack invItem : player.getInventory().getContents()) {
                            if (invItem == null) {
                                continue;
                            }

                            String itemId = NexoItems.idFromItem(invItem);
                            if (!hasMagnet(itemId)) {
                                continue;
                            }
                            if (!isActive(invItem)) {
                                continue;
                            }

                            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
                            Magnet magnet = mechanics.getMagnet();

                            pullItemsToPlayer(player, magnet);
                            break; // Ein aktiver Magnet pro Spieler genügt
                        }
                    }
                }
            }.runTaskTimer(NexoAddon.getInstance(), 0L, 2L);
        }

        private static void pullItemsToPlayer(Player player, Magnet magnet) {

            if (player.getWorld().getName().equalsIgnoreCase("Plots")) {
                return;
            }

            Collection<Entity> nearby = player.getWorld().getNearbyEntities(
                player.getLocation(), magnet.radius(), magnet.radius(), magnet.radius()
            );

            boolean pulledAny = false;
            for (Entity entity : nearby) {
                if (entity.getType() != EntityType.ITEM) {
                    continue;
                }

                Item item = (Item) entity;

                Vector direction = player.getLocation().toVector()
                    .subtract(item.getLocation().toVector());

                double distance = direction.length();
                if (distance < 0.5) {
                    continue;
                }

                direction.normalize().multiply(magnet.pullSpeed());
                item.setVelocity(direction);

                if (magnet.particleType() != null && !magnet.particleType().isEmpty()) {
                    try {
                        Particle particle = Particle.valueOf(magnet.particleType().toUpperCase());
                        item.getWorld().spawnParticle(particle, item.getLocation(),
                            magnet.particleAmount(), 0.1, 0.1, 0.1, 0.0);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                pulledAny = true;
            }

            if (pulledAny && magnet.soundType() != null && !magnet.soundType().isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(magnet.soundType().toUpperCase());
                    player.getWorld().playSound(player.getLocation(), sound,
                        magnet.soundVolume(), magnet.soundPitch());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
