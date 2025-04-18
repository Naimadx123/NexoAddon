package zone.vao.nexoAddon.events.chunk;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {

    FurniturePopulator.onLoad(event);
  }
}
