package zone.vao.nexoAddon.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import zone.vao.nexoAddon.NexoAddon;

public class WorldLoadListener implements Listener {

  @EventHandler
  public void onWorldInit(WorldInitEvent event) {
    NexoAddon.getInstance().getPopulatorManager().attach(event.getWorld());
  }

  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {
    NexoAddon.getInstance().getPopulatorManager().attach(event.getWorld());
  }

  @EventHandler(ignoreCancelled = true)
  public void onWorldUnload(WorldUnloadEvent event) {
    NexoAddon.getInstance().getPopulatorManager().detach(event.getWorld());
  }
}
