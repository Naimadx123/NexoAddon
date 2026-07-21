package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.BlockUtil;

import java.util.List;


public record Spread(
    int interval,
    double chance,
    int radius,
    List<Material> replace,
    boolean requiresAirAbove,
    int maxNearby,
    int lightMin,
    int lightMax,
    List<String> biomes,
    String result
) {

  public static class SpreadListener implements Listener {

    @EventHandler
    public void onNexoBlockPlace(NexoBlockPlaceEvent event) {
      if (event.isCancelled() || NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getSpread() == null) return;

      BlockUtil.startSpread(event.getBlock().getLocation());
    }

    @EventHandler
    public void onNexoBlockBreak(NexoBlockBreakEvent event) {
      if (event.isCancelled()) return;

      BlockUtil.stopSpread(event.getBlock().getLocation());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
      if (!NexoAddon.getInstance().getIsSpread()) return;

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(
          event.getChunk().getBlock(0, 0, 0).getLocation(),
          r -> BlockUtil.restartSpread(event.getChunk()),
          3L
      );
    }
  }
}
