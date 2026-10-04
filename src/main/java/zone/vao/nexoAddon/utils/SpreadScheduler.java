package zone.vao.nexoAddon.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.Spread;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpreadScheduler {

  private static final class Entry {
    final Location location;
    final Spread spread;
    final String sourceId;
    volatile long nextDueTick;

    Entry(Location location, Spread spread, String sourceId, long nextDueTick) {
      this.location = location;
      this.spread = spread;
      this.sourceId = sourceId;
      this.nextDueTick = nextDueTick;
    }
  }

  private record Pending(Entry entry, long dueTick) {}

  private final Map<Location, Entry> entries = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean ticking = new AtomicBoolean(false);
  private volatile int maxPerTick;
  private volatile WrappedTask tickerTask;
  private long currentTick = 0L;

  public SpreadScheduler(int maxPerTick) {
    this.maxPerTick = Math.max(1, maxPerTick);
  }

  public static NamespacedKey spreadKey() {
    return new NamespacedKey(NexoAddon.getInstance(), "spread");
  }

  public void setMaxPerTick(int maxPerTick) {
    this.maxPerTick = Math.max(1, maxPerTick);
  }

  public boolean isRegistered(Location location) {
    return location != null && entries.containsKey(location);
  }

  public void register(Location location, Spread spread, String sourceId) {
    register(location, spread, sourceId, true);
  }

  public void register(Location location, Spread spread, String sourceId, boolean writeMarker) {
    if (location == null || location.getWorld() == null || spread == null || sourceId == null) return;
    if (entries.putIfAbsent(location, new Entry(location, spread, sourceId,
        currentTick + 1 + ThreadLocalRandom.current().nextLong(spread.interval()))) != null) {
      return;
    }

    if (writeMarker) {
      CustomBlockData data = new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
      data.set(spreadKey(), PersistentDataType.STRING, sourceId);
    }
    ensureRunning();
  }

  public void unregister(Location location) {
    if (location == null) return;
    entries.remove(location);
    if (location.getWorld() == null) return;
    CustomBlockData data = new CustomBlockData(location.getBlock(), NexoAddon.getInstance());
    data.remove(spreadKey());
  }

  public void forgetChunk(Chunk chunk) {
    if (chunk == null) return;
    int cx = chunk.getX();
    int cz = chunk.getZ();
    String world = chunk.getWorld().getName();
    entries.keySet().removeIf(loc -> loc.getWorld() != null
        && loc.getWorld().getName().equals(world)
        && (loc.getBlockX() >> 4) == cx
        && (loc.getBlockZ() >> 4) == cz);
  }

  public void start() {
    ensureRunning();
  }

  public void stop() {
    running.set(false);
    WrappedTask task = tickerTask;
    if (task != null) {
      task.cancel();
      tickerTask = null;
    }
    entries.clear();
  }

  private void ensureRunning() {
    if (!running.compareAndSet(false, true)) return;
    tickerTask = NexoAddon.getInstance().foliaLib.getScheduler().runTimerAsync(this::tick, 1L, 1L);
  }

  private void tick() {
    if (!ticking.compareAndSet(false, true)) return;
    try {
      long tick = ++currentTick;
      if (entries.isEmpty()) return;

      List<Entry> due = null;
      for (Entry entry : entries.values()) {
        if (entry.nextDueTick <= tick) {
          if (due == null) due = new ArrayList<>();
          due.add(entry);
        }
      }
      if (due == null) return;

      int budget = maxPerTick;
      if (due.size() <= budget) {
        for (Entry entry : due) dispatch(entry, tick);
        return;
      }

      List<Pending> pending = new ArrayList<>(due.size());
      for (Entry entry : due) pending.add(new Pending(entry, entry.nextDueTick));
      pending.sort(Comparator.comparingLong(Pending::dueTick));

      for (int i = 0; i < budget; i++) dispatch(pending.get(i).entry(), tick);
    } finally {
      ticking.set(false);
    }
  }

  private void dispatch(Entry entry, long tick) {
    entry.nextDueTick = tick + entry.spread.interval();
    NexoAddon.getInstance().foliaLib.getScheduler().runAtLocation(entry.location, r -> attempt(entry));
  }

  private void attempt(Entry entry) {
    if (ThreadLocalRandom.current().nextDouble() > entry.spread.chance()) return;

    Block block = entry.location.getBlock();
    if (!NexoBlocks.isCustomBlock(block)) {
      unregister(entry.location);
      return;
    }

    CustomBlockMechanic mechanic = NexoBlocks.customBlockMechanic(entry.location);
    if (mechanic == null || !entry.sourceId.equals(mechanic.getItemID())) {
      unregister(entry.location);
      return;
    }

    BlockUtil.trySpread(block, entry.spread, entry.sourceId);
  }
}
