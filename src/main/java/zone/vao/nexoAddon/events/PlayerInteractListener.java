package zone.vao.nexoAddon.events;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import zone.vao.nexoAddon.events.playerInteracts.EquippableListener;
import zone.vao.nexoAddon.events.playerInteracts.JukeboxPlayableListener;


public class PlayerInteractListener implements Listener {

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {

    EquippableListener.onEquippable(event);
    JukeboxPlayableListener.onJukeboxPlayable(event);
  }
}
