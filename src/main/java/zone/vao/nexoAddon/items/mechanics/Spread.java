package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.BlockUtil;
import zone.vao.nexoAddon.utils.SpreadScheduler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public record Spread(
    int interval,
    double chance,
    int radius,
    boolean requiresAirAbove,
    int maxNearby,
    int lightMin,
    int lightMax,
    List<String> biomes,
    Mode mode,
    boolean protectionEnabled,
    boolean respectClaims,
    List<Rule> rules
) {

  public enum Mode {
    SINGLE,
    MULTI
  }

  public record Rule(
      boolean wildcard,
      Set<Material> materials,
      List<Tag<Material>> tags,
      String result
  ) {

    public boolean matches(Material material) {
      if (wildcard) return true;
      if (materials.contains(material)) return true;

      for (Tag<Material> tag : tags) {
        if (tag.isTagged(material)) return true;
      }
      return false;
    }
  }

  public Rule ruleFor(Material material) {
    for (Rule rule : rules) {
      if (rule.matches(material)) return rule;
    }
    return null;
  }

  public boolean hasNearbyLimit() {
    return maxNearby > 0;
  }

  public Set<String> resultIds(String sourceId) {
    Set<String> ids = new LinkedHashSet<>();
    for (Rule rule : rules) {
      ids.add(resolveResult(rule.result(), sourceId));
    }
    return ids;
  }

  public static String resolveResult(String result, String sourceId) {
    return result == null || "self".equalsIgnoreCase(result) ? sourceId : result;
  }

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

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
      if (!NexoAddon.getInstance().getIsSpread()) return;

      SpreadScheduler scheduler = NexoAddon.getInstance().getSpreadScheduler();
      if (scheduler != null) scheduler.forgetChunk(event.getChunk());
    }
  }
}
