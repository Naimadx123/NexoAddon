package zone.vao.nexoAddon.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Each change owns its callbacks, even if the same position is reused before its timer expires. */
final class ShiftBlockTracker {
  private final Map<Location, Change> changes = new ConcurrentHashMap<>();

  Change begin(Location location) {
    Change change = new Change();
    return changes.putIfAbsent(key(location), change) == null ? change : null;
  }

  boolean isCurrent(Location location, Change change) {
    return changes.get(key(location)) == change;
  }

  boolean finish(Location location, Change change) {
    if (!changes.remove(key(location), change)) return false;
    change.cancel();
    return true;
  }

  boolean clear(Location location) {
    Change change = changes.remove(key(location));
    if (change == null) return false;
    change.cancel();
    return true;
  }

  Set<Location> locations() {
    return changes.keySet();
  }

  private Location key(Location location) {
    return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  static final class Change {
    private final ArrayList<ScheduledTask> tasks = new ArrayList<>();

    void track(ScheduledTask task) {
      tasks.add(task);
    }

    private void cancel() {
      tasks.forEach(ScheduledTask::cancel);
      tasks.clear();
    }
  }
}
