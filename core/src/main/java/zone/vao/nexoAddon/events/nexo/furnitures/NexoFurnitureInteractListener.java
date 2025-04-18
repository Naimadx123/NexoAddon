package zone.vao.nexoAddon.events.nexo.furnitures;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.events.nexo.furnitures.interacts.DisplayCropsHologram;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NexoFurnitureInteractListener implements Listener {

  private final ConcurrentHashMap<UUID, Long> recentEvents = new ConcurrentHashMap<>();
  private static final long EVENT_COOLDOWN_MS = 100;

  @EventHandler
  public void onPlayerInteractFurniture(NexoFurnitureInteractEvent event) {
    Player player = event.getPlayer();

    UUID playerUUID = player.getUniqueId();
    long currentTime = System.currentTimeMillis();
    if (recentEvents.containsKey(playerUUID) && (currentTime - recentEvents.get(playerUUID)) < EVENT_COOLDOWN_MS) {
      return;
    }

    recentEvents.put(playerUUID, currentTime);

    DisplayCropsHologram.onInteract(event);
  }

}
