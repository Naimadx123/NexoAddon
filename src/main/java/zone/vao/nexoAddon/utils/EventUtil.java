package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import zone.vao.nexoAddon.NexoAddon;

public class EventUtil {

  public static void runAfterBlockBreak(BlockBreakEvent event, Runnable action) {
    if (!NexoAddon.getInstance().getGlobalConfig().getBoolean("check_break_for_next_tick", false)) {
      action.run();
      return;
    }

    Runnable task = () -> {
      if (!event.isCancelled()) action.run();
    };

    Bukkit.getRegionScheduler().run(NexoAddon.getInstance(), event.getBlock().getLocation(), attempt -> task.run());
  }

  public static boolean callEvent(Event event) {
    Bukkit.getPluginManager().callEvent(event);
    if (event instanceof Cancellable cancellable) return !cancellable.isCancelled();
    else return true;
  }
}
