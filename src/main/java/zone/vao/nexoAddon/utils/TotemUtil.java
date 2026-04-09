package zone.vao.nexoAddon.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.nexomc.nexo.api.NexoItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class TotemUtil {

    public static void playTotemAnimation(Player player, int customModelData, String sound) {
        org.bukkit.inventory.ItemStack bukkitItem = new org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = bukkitItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            bukkitItem.setItemMeta(meta);
        }
        sendCustomTotemAnimation(player, bukkitItem, sound);
    }

    public static void playTotemAnimation(Player player, String nexoID, String sound) {
        if (NexoItems.itemFromId(nexoID) == null) return;
        org.bukkit.inventory.ItemStack totem = NexoItems.itemFromId(nexoID).build();
        sendCustomTotemAnimation(player, totem, sound);
    }

    private static void sendCustomTotemAnimation(Player player, org.bukkit.inventory.ItemStack bukkitItem, String sound) {
        if (bukkitItem.getType() != Material.TOTEM_OF_UNDYING)
            throw new IllegalArgumentException("ItemStack " + bukkitItem + " isn't a Totem of Undying!");

        ItemStack packetItem = SpigotConversionUtil.fromBukkitItemStack(bukkitItem);

        WrapperPlayServerSetSlot setSlotPacket = new WrapperPlayServerSetSlot(
                0,
                0,
                45,
                packetItem
        );

        WrapperPlayServerEntityStatus entityStatusPacket = new WrapperPlayServerEntityStatus(
                player.getEntityId(),
                (byte) 35
        );

      Sound sound1 = Sounds.getByNameOrCreate(sound);

      WrapperPlayServerSoundEffect soundEffectPacket = new WrapperPlayServerSoundEffect(
          sound1,
          SoundCategory.AMBIENT,
          new Vector3i(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()),
          1f,
          1f
      );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, soundEffectPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, setSlotPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, entityStatusPacket);

        player.updateInventory();
    }
}