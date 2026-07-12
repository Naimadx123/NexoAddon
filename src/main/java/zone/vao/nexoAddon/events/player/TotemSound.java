package zone.vao.nexoAddon.events.player;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.VersionUtil;

public class TotemSound implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        String soundKey = NexoAddon.getInstance().getGlobalConfig().getString("default_totem_sound", "minecraft:entity.totem.use");
        if (soundKey.equalsIgnoreCase("minecraft:entity.totem.use")) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        ItemStack used = null;

        if (isUndyingItem(main)) used = main;
        else if (isUndyingItem(off)) used = off;

        if (used == null) return;

        Sound sound = Sound.sound(
                Key.key(soundKey),
                Sound.Source.PLAYER,
                1f,
                1f
        );

        player.playSound(sound);
    }

    private boolean isUndyingItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        if (item.getType() == Material.TOTEM_OF_UNDYING) return true;

        if(!VersionUtil.isVersionLessThan("1.21.2"))
            return item.hasData(DataComponentTypes.DEATH_PROTECTION);
        return false;
    }
}
