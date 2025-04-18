package zone.vao.nexoAddon.items.components;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import io.th0rgal.protectionlib.ProtectionLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.utils.InventoryUtil;
import zone.vao.nexoAddon.utils.VersionUtil;

public record JukeboxPlayable(String songKey) {

  public static class JukeboxPlayableListener implements Listener {

    @EventHandler
    public static void onBlockBreak(BlockBreakEvent event){
      if(event.getBlock().getType() == Material.JUKEBOX){

        String soundKey = NexoAddon.getInstance().jukeboxLocations.get(event.getBlock().getLocation().toString());
        if(soundKey == null || !ProtectionLib.canBreak(event.getPlayer(), event.getBlock().getLocation())) return;

        Jukebox jukebox = (Jukebox) event.getBlock().getState();

        if(jukebox.getRecord().getItemMeta() == null) return;

        String itemId = jukebox.getRecord().getItemMeta().getDisplayName();
        ItemBuilder itemBuilder = NexoItems.itemFromId(itemId);
        if(itemBuilder == null) return;
        jukebox.setRecord(null);
        jukebox.update();
        jukebox.getWorld().dropItemNaturally(jukebox.getLocation(), itemBuilder.build().clone());

        event.getBlock().getWorld().getNearbyEntities(jukebox.getLocation(), 16,16,16).forEach(entity -> {
          if(entity instanceof Player player){
            player.stopSound(soundKey, SoundCategory.RECORDS);
          }
        });
        NexoAddon.getInstance().jukeboxLocations.remove(event.getBlock().getLocation().toString());
      }
    }

    @EventHandler
    public static void onJukeboxPlayable(final PlayerInteractEvent event) {
      if (!VersionUtil.isVersionLessThan("1.21.1")) return;

      Player player = event.getPlayer();
      if (!isValidInteraction(event) || event.getClickedBlock() == null) return;

      Jukebox jukebox = (Jukebox) event.getClickedBlock().getState();

      if (jukebox.hasRecord() && handleEjectRecord(jukebox, event)) return;

      if(jukebox.hasRecord() && jukebox.getRecord().getItemMeta() != null){
        String itemId = jukebox.getRecord().getItemMeta().getDisplayName();
        ItemBuilder itemBuilder = NexoItems.itemFromId(itemId);
        if (itemBuilder == null) return;
      }

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

    private static boolean handleEjectRecord(Jukebox jukebox, PlayerInteractEvent event) {
      if(jukebox.getRecord().getItemMeta() == null) return false;
      String itemId = jukebox.getRecord().getItemMeta().getDisplayName();
      ItemBuilder itemBuilder = NexoItems.itemFromId(itemId);
      if (itemBuilder == null) return false;

      Components component = NexoAddon.getInstance().getComponents().get(itemId);
      if (component != null && component.getPlayable() != null) {
        jukebox.getWorld().getNearbyEntities(jukebox.getLocation(), 16,16,16).forEach(entity -> {
          if(entity instanceof Player player){
            player.stopSound(component.getPlayable().songKey(), SoundCategory.RECORDS);
          }
        });
      }

      event.setCancelled(true);
      jukebox.setRecord(null);
      jukebox.update();
      jukebox.getWorld().dropItemNaturally(jukebox.getLocation().add(0,1,0), itemBuilder.build().clone());
      NexoAddon.getInstance().jukeboxLocations.remove(jukebox.getLocation().toString());
      return true;
    }

    private static void handleInsertRecord(PlayerInteractEvent event, Player player, Jukebox jukebox) {
      String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
      if (itemId == null || !VersionUtil.isVersionLessThan("1.21.1")) return;

      Components component = NexoAddon.getInstance().getComponents().get(itemId);
      if (component == null || component.getPlayable() == null) return;

      event.setCancelled(true);

      ItemStack item = player.getInventory().getItemInMainHand().clone();

      ItemStack is = new ItemStack(Material.MUSIC_DISC_CAT);
      ItemMeta meta = is.getItemMeta();
      meta.setDisplayName(itemId);
      is.setItemMeta(meta);
      new BukkitRunnable(){

        @Override
        public void run() {
          jukebox.setRecord(is);
          jukebox.update();
          if(item.getItemMeta() != null && item.getItemMeta().getLore() != null)
            Audience.audience(player)
                .sendActionBar(MiniMessage.miniMessage().deserialize("Now Playing: "+item.getItemMeta().getLore().getFirst()));
          else
            Audience.audience(player)
                .sendActionBar(MiniMessage.miniMessage().deserialize("§r"));
        }
      }.runTaskLater(NexoAddon.getInstance(), 1L);

      new BukkitRunnable(){

        @Override
        public void run() {
          jukebox.stopPlaying();
        }
      }.runTaskLater(NexoAddon.getInstance(), 2L);

      jukebox.getWorld().playSound(
          jukebox.getLocation(),
          component.getPlayable().songKey(),
          SoundCategory.RECORDS,
          1f,
          1f
      );

      NexoAddon.getInstance().jukeboxLocations.put(jukebox.getLocation().toString(), component.getPlayable().songKey());
      InventoryUtil.removePartialStack(player, player.getInventory().getItemInMainHand(), 1);
    }
  }
}
