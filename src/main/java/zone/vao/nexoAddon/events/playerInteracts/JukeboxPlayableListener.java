package zone.vao.nexoAddon.events.playerInteracts;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Components;
import zone.vao.nexoAddon.utils.InventoryUtil;
import zone.vao.nexoAddon.utils.VersionUtil;

public class JukeboxPlayableListener {

  public static void onJukeboxPlayable(final PlayerInteractEvent event) {
    if (!VersionUtil.isVersionLessThan("1.21")) return;

    Player player = event.getPlayer();
    if (!isValidInteraction(event)) return;

    Jukebox jukebox = (Jukebox) event.getClickedBlock().getState();

    if (jukebox.hasRecord() && handleEjectRecord(jukebox)) return;

    handleInsertRecord(event, player, jukebox);
  }

  private static boolean isValidInteraction(PlayerInteractEvent event) {
    return event.getHand() == EquipmentSlot.HAND
        && event.getClickedBlock() != null
        && event.getClickedBlock().getType() == Material.JUKEBOX
        && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        && ProtectionLib.canInteract(event.getPlayer(), event.getClickedBlock().getLocation())
        && ProtectionLib.canUse(event.getPlayer(), event.getClickedBlock().getLocation())
        && event.getClickedBlock().getState() instanceof Jukebox;
  }

  private static boolean handleEjectRecord(Jukebox jukebox) {
    String recordId = NexoItems.idFromItem(jukebox.getRecord());
    if (recordId == null) return false;

    Components component = NexoAddon.getInstance().getComponents().get(recordId);
    if (component != null && component.getPlayable() != null) {
      Bukkit.getOnlinePlayers().forEach(player ->
          player.stopSound(component.getPlayable().getSongKey(), SoundCategory.RECORDS));
    }

    jukebox.eject();
    NexoAddon.getInstance().jukeboxLocations.remove(jukebox.getLocation().toString());
    return true;
  }

  private static void handleInsertRecord(PlayerInteractEvent event, Player player, Jukebox jukebox) {
    String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
    if (itemId == null || !NexoAddon.getInstance().isComponentSupport()) return;

    Components component = NexoAddon.getInstance().getComponents().get(itemId);
    if (component == null || component.getPlayable() == null) return;

    event.setCancelled(true);

    ItemStack item = player.getInventory().getItemInMainHand().clone();
    item.setAmount(1);
    jukebox.setRecord(item);
    jukebox.update();

    jukebox.getWorld().playSound(
        jukebox.getLocation(),
        component.getPlayable().getSongKey(),
        SoundCategory.RECORDS,
        1f,
        1f
    );

    NexoAddon.getInstance().jukeboxLocations.put(jukebox.getLocation().toString(), component.getPlayable().getSongKey());
    InventoryUtil.removePartialStack(player, 1);
  }
}
