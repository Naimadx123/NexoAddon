package zone.vao.nexoAddon.events;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import zone.vao.nexoAddon.NexoAddon;

public class BlockBreakListener implements Listener {

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    if(event.getBlock().getType() == Material.JUKEBOX){

      String soundKey = NexoAddon.getInstance().jukeboxLocations.get(event.getBlock().getLocation().toString());
      if(soundKey == null) return;

      event.getBlock().getWorld().getPlayers().forEach(player -> player.stopSound(soundKey, SoundCategory.RECORDS));
      NexoAddon.getInstance().jukeboxLocations.remove(event.getBlock().getLocation().toString());
    }
  }
}
