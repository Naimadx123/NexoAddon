package zone.vao.nexoAddon.events.nexo.furnitures.breaks;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import lombok.Getter;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.handlers.ApiCompatibilityHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BreakDoubleHit {

  private static final long TIMEFRAME = 500;
  private static final Map<UUID, FurnitureTimestamp> lastHits = new HashMap<>();

  public static void onDoubleHitMechanic(NexoFurnitureBreakEvent event) {

    if(!NexoAddon.getInstance().getGlobalConfig().getBoolean("double_hit_destroy_mechanic", true)
    || ApiCompatibilityHandler.hasEvolution(event.getMechanic())) return;

    event.getMechanic().getHitbox().refreshHitboxes(event.getBaseEntity(), event.getMechanic());

    UUID playerId = event.getPlayer().getUniqueId();
    UUID furnitureId = event.getBaseEntity().getUniqueId();

    FurnitureTimestamp lastHit = lastHits.get(playerId);

    if (lastHit == null || !lastHit.getFurniture().equals(furnitureId)
        || (lastHit.getTimestamp() + TIMEFRAME) < System.currentTimeMillis()) {
      lastHits.put(playerId, new FurnitureTimestamp(furnitureId));
      event.setCancelled(true);
    } else {
      lastHits.remove(playerId);
    }
  }

  @Getter
  private static class FurnitureTimestamp {
    private final long timestamp = System.currentTimeMillis();
    private final UUID furniture;

    public FurnitureTimestamp(UUID furniture) {
      this.furniture = furniture;
    }

  }
}
