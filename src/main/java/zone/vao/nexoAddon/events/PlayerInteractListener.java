package zone.vao.nexoAddon.events;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import zone.vao.nexoAddon.events.playerInteracts.*;


public class PlayerInteractListener implements Listener {

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {

    AdventureGamemodeSupport.onInteract(event);
    EquippableListener.onEquippable(event);
    JukeboxPlayableListener.onJukeboxPlayable(event);
    FertilizeVanillaCrops.fertilizeVanillaCrops(event);
    BigMiningToggle.onToggle(event);
  }
}
