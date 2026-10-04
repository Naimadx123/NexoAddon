package zone.vao.nexoAddon.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownUtil {

  private static final Map<String, Map<UUID, Long>> pools = new ConcurrentHashMap<>();

  public static boolean tryConsume(String pool, UUID id, double seconds) {
    if (pool == null || id == null) return true;
    if (seconds <= 0) return true;

    Map<UUID, Long> cooldowns = pools.computeIfAbsent(pool, key -> new ConcurrentHashMap<>());
    long now = System.currentTimeMillis();
    long readyAt = now + (long) (seconds * 1000L);

    boolean[] consumed = {false};
    cooldowns.compute(id, (key, current) -> {
      if (current != null && current > now) return current;
      consumed[0] = true;
      return readyAt;
    });

    return consumed[0];
  }

  public static void forget(UUID id) {
    if (id == null) return;
    pools.values().forEach(cooldowns -> cooldowns.remove(id));
  }

  public static void clearAll() {
    pools.clear();
  }
}
