package zone.vao.nexoAddon.events.nexo.furnitures;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.events.nexo.furnitures.breaks.BreakDoubleHit;
import zone.vao.nexoAddon.events.nexo.furnitures.breaks.BreakWithToolsListener;


public class NexoFurnitureBreakListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  public void onFurnitureBreak(NexoFurnitureBreakEvent event) {

    BreakWithToolsListener.onBreakWithTools(event);
    BreakDoubleHit.onDoubleHitMechanic(event);
  }
}
