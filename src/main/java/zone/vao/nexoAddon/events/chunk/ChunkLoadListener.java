package zone.vao.nexoAddon.events.chunk;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import zone.vao.nexoAddon.NexoAddon;

public class ChunkLoadListener implements Listener {

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {

    if (event.isNewChunk()) NexoAddon.getInstance().getPopulatorManager().populateDeferred(event.getChunk());
  }
}
