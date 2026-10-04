package zone.vao.nexoAddon.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BreakCascade {

  private static final Map<UUID, Integer> active = new ConcurrentHashMap<>();

  public static boolean isActive(UUID id) {
    if (id == null) return false;
    return active.containsKey(id);
  }

  public static void hold(UUID id) {
    if (id == null) return;
    active.merge(id, 1, Integer::sum);
  }

  public static void release(UUID id) {
    if (id == null) return;
    active.computeIfPresent(id, (key, holds) -> holds <= 1 ? null : holds - 1);
  }

  public static void clearAll() {
    active.clear();
  }
}
