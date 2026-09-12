package zone.vao.nexoAddon.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.Liquid;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class LiquidUtil {

  private record ChunkRef(UUID world, long chunk) {}

  private static final Map<NamespacedKey, Liquid> liquids = new ConcurrentHashMap<>();
  private static final Map<ChunkRef, Long> lastRefresh = new ConcurrentHashMap<>();
  private static final Set<ChunkRef> pendingRefresh = ConcurrentHashMap.newKeySet();
  private static final Map<ChunkRef, Map<Location, Block>> tinted = new ConcurrentHashMap<>();
  private static volatile WrappedTask sweepTask;

  public static Biome resolveBiome(String raw) {
    if (raw == null || raw.isBlank()) return null;

    try {
      NamespacedKey key = raw.contains(":") ? NamespacedKey.fromString(raw) : NamespacedKey.minecraft(raw);
      if (key == null) return null;

      return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
    } catch (Throwable throwable) {
      return null;
    }
  }

  public static boolean register(Liquid liquid) {
    if (liquid == null || liquid.biome() == null) return false;

    NamespacedKey key = liquid.biome().getKey();
    Liquid existing = liquids.get(key);
    if (existing != null) {
      String kept = existing.itemId().compareTo(liquid.itemId()) <= 0 ? existing.itemId() : liquid.itemId();
      NexoAddon.getInstance().getLogger().warning("Biome " + key
          + " is claimed by both `" + existing.itemId() + "` and `" + liquid.itemId()
          + "`. Using `" + kept + "`.");
      if (!kept.equals(liquid.itemId())) return false;
    }

    liquids.put(key, liquid);
    return true;
  }

  public static Liquid byBiome(Biome biome) {
    return biome == null ? null : liquids.get(biome.getKey());
  }

  public static void clear() {
    liquids.clear();
    lastRefresh.clear();
    pendingRefresh.clear();
    tinted.clear();
    WrappedTask task = sweepTask;
    if (task != null) {
      task.cancel();
      sweepTask = null;
    }
  }

  private static void track(World world, int x, int y, int z) {
    ChunkRef ref = new ChunkRef(world.getUID(), packChunk(x >> 4, z >> 4));
    Block anchor = cellAnchor(world, x, y, z);
    tinted.computeIfAbsent(ref, key -> new ConcurrentHashMap<>()).putIfAbsent(anchor.getLocation(), anchor);
    ensureSweeping();
  }

  public static void restart(Chunk chunk) {
    for (Block block : CustomBlockData.getBlocksWithCustomData(NexoAddon.getInstance(), chunk)) {
      CustomBlockData data = new CustomBlockData(block, NexoAddon.getInstance());
      if (!data.has(originalBiomeKey(), PersistentDataType.STRING)) continue;
      track(chunk.getWorld(), block.getX(), block.getY(), block.getZ());
    }
  }

  public static void restartLoaded() {
    NexoAddon.getInstance().getFoliaLib().getScheduler().runLater(() -> {
      for (World world : Bukkit.getWorlds()) {
        for (Chunk chunk : world.getLoadedChunks()) restart(chunk);
      }
    }, 10L);
  }

  public static void forgetChunk(Chunk chunk) {
    tinted.remove(new ChunkRef(chunk.getWorld().getUID(), packChunk(chunk.getX(), chunk.getZ())));
  }

  private static void ensureSweeping() {
    if (sweepTask != null) return;
    long interval = Math.max(1L,
        NexoAddon.getInstance().getGlobalConfig().getLong("liquid.restore_interval_ticks", 20));
    sweepTask = NexoAddon.getInstance().getFoliaLib().getScheduler().runTimer(LiquidUtil::sweep, interval, interval);
  }

  private static void sweep() {
    for (Map.Entry<ChunkRef, Map<Location, Block>> entry : tinted.entrySet()) {
      ChunkRef ref = entry.getKey();
      Map<Location, Block> cells = entry.getValue();
      World world = Bukkit.getWorld(ref.world());
      Location anchor = cells.keySet().stream().findFirst().orElse(null);
      if (world == null || anchor == null) {
        tinted.remove(ref);
        continue;
      }
      if (!world.isChunkLoaded((int) (ref.chunk() >> 32), (int) ref.chunk())) continue;

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocation(anchor, task -> {
        for (Map.Entry<Location, Block> cell : cells.entrySet()) {
          if (holdsLiquid(cell.getValue())) continue;

          Block water = sweepCell(cell.getKey());
          if (water == null) cells.remove(cell.getKey());
          else cells.put(cell.getKey(), water);
        }
      });
    }
  }

  private static Block sweepCell(Location cell) {
    Block block = cell.getBlock();
    Liquid owner = byBiome(block.getBiome());
    if (owner == null) {
      new CustomBlockData(block, NexoAddon.getInstance()).remove(originalBiomeKey());
      return null;
    }

    Block water = cellFind(block.getWorld(), block.getX(), block.getY(), block.getZ(),
        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, LiquidUtil::holdsLiquid);
    if (water != null) return water;

    return restoreCell(cell, owner.revertTo(), false) == RESTORE_OK ? null : block;
  }

  public static boolean isWater(Block block) {
    if (block == null) return false;
    if (block.getType() == Material.WATER) return true;
    return block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged();
  }

  public static boolean holdsLiquid(Block block) {
    if (block == null) return false;
    return block.getType() == Material.WATER_CAULDRON || isWater(block);
  }

  private static NamespacedKey originalBiomeKey() {
    return new NamespacedKey(NexoAddon.getInstance(), "liquid_original_biome");
  }

  private static Block cellAnchor(World world, int x, int y, int z) {
    return world.getBlockAt((x >> 2) << 2, (y >> 2) << 2, (z >> 2) << 2);
  }

  private static void rememberOriginal(World world, int x, int y, int z, Biome current) {
    if (byBiome(current) != null) return;

    CustomBlockData data = new CustomBlockData(cellAnchor(world, x, y, z), NexoAddon.getInstance());
    if (data.has(originalBiomeKey(), PersistentDataType.STRING)) return;

    data.set(originalBiomeKey(), PersistentDataType.STRING, current.getKey().toString());
  }

  public static boolean isSource(Block block) {
    if (!holdsLiquid(block)) return false;
    if (block.getType() == Material.WATER_CAULDRON) return true;
    return !(block.getBlockData() instanceof Levelled levelled) || levelled.getLevel() == 0;
  }

  public static boolean cellHasWater(World world, int x, int y, int z) {
    return cellFind(world, x, y, z, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, LiquidUtil::holdsLiquid) != null;
  }

  public static boolean cellHasSource(World world, int x, int y, int z, int skipX, int skipY, int skipZ) {
    return cellFind(world, x, y, z, skipX, skipY, skipZ, LiquidUtil::isSource) != null;
  }

  private static Block cellFind(World world, int x, int y, int z, int skipX, int skipY, int skipZ,
                                java.util.function.Predicate<Block> test) {
    int baseX = (x >> 2) << 2;
    int baseY = (y >> 2) << 2;
    int baseZ = (z >> 2) << 2;

    for (int dx = 0; dx < 4; dx++) {
      for (int dy = 0; dy < 4; dy++) {
        for (int dz = 0; dz < 4; dz++) {
          int cx = baseX + dx;
          int cy = baseY + dy;
          int cz = baseZ + dz;
          if (cy < world.getMinHeight() || cy >= world.getMaxHeight()) continue;
          if (cx == skipX && cy == skipY && cz == skipZ) continue;
          Block block = world.getBlockAt(cx, cy, cz);
          if (test.test(block)) return block;
        }
      }
    }
    return null;
  }

  public record CleanupResult(int restored, int hadWater, int noOriginal) {}

  public static void cleanup(Location center, int radiusBlocks, Biome forced,
                             java.util.function.Consumer<CleanupResult> onDone) {
    World world = center == null ? null : center.getWorld();
    if (world == null) {
      onDone.accept(new CleanupResult(0, 0, 0));
      return;
    }

    int radius = Math.max(1, Math.min(radiusBlocks, 64));
    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    java.util.List<int[]> cells = new java.util.ArrayList<>();
    Set<Long> seen = new HashSet<>();

    for (int x = centerX - radius; x <= centerX + radius; x += 1) {
      for (int y = centerY - radius; y <= centerY + radius; y += 1) {
        for (int z = centerZ - radius; z <= centerZ + radius; z += 1) {
          if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;
          if (!seen.add(packCell(x >> 2, y >> 2, z >> 2))) continue;
          if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
          if (byBiome(world.getBiome(x, y, z)) == null) continue;

          cells.add(new int[]{x, y, z});
        }
      }
    }

    processCleanup(world, cells, 0, new int[3], forced, onDone);
  }

  private static void processCleanup(World world, java.util.List<int[]> cells, int index, int[] tally,
                                    Biome forced, java.util.function.Consumer<CleanupResult> onDone) {
    int limit = Math.min(cells.size(), index + 256);

    for (int i = index; i < limit; i++) {
      int[] cell = cells.get(i);
      int status = restoreCell(new Location(world, cell[0], cell[1], cell[2]), forced, forced != null);

      if (status == RESTORE_OK) tally[0]++;
      else if (status == RESTORE_HAS_WATER) tally[1]++;
      else if (status == RESTORE_NO_ORIGINAL) tally[2]++;
    }

    if (limit >= cells.size()) {
      onDone.accept(new CleanupResult(tally[0], tally[1], tally[2]));
      return;
    }

    int[] anchor = cells.get(limit);
    NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(
        new Location(world, anchor[0], anchor[1], anchor[2]),
        task -> processCleanup(world, cells, limit, tally, forced, onDone), 1L);
  }

  private static Biome neighbourBiome(World world, int x, int y, int z) {
    int[] offsets = {4, -4, 8, -8, 12, -12};
    for (int offset : offsets) {
      for (int axis = 0; axis < 3; axis++) {
        int nx = axis == 0 ? x + offset : x;
        int ny = axis == 1 ? y + offset : y;
        int nz = axis == 2 ? z + offset : z;

        if (ny < world.getMinHeight() || ny >= world.getMaxHeight()) continue;
        if (!world.isChunkLoaded(nx >> 4, nz >> 4)) continue;

        Biome candidate = world.getBiome(nx, ny, nz);
        if (byBiome(candidate) == null) return candidate;
      }
    }
    return null;
  }

  public static final int RESTORE_OK = 0;
  public static final int RESTORE_NOT_LIQUID = 1;
  public static final int RESTORE_HAS_WATER = 2;
  public static final int RESTORE_NO_ORIGINAL = 3;

  public static int restoreCell(Location location, Biome fallback, boolean force) {
    if (location == null) return RESTORE_NOT_LIQUID;

    World world = location.getWorld();
    if (world == null) return RESTORE_NOT_LIQUID;

    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();

    if (byBiome(world.getBiome(x, y, z)) == null) return RESTORE_NOT_LIQUID;
    if (cellHasWater(world, x, y, z)) return RESTORE_HAS_WATER;

    CustomBlockData data = new CustomBlockData(cellAnchor(world, x, y, z), NexoAddon.getInstance());
    String stored = data.get(originalBiomeKey(), PersistentDataType.STRING);

    Biome original = force ? fallback : null;
    if (original == null && stored != null) original = resolveBiome(stored);
    if (original == null) original = fallback;
    if (original == null) original = neighbourBiome(world, x, y, z);
    if (original == null) return RESTORE_NO_ORIGINAL;

    Set<Long> chunks = new HashSet<>();
    world.setBiome(x, y, z, original);
    chunks.add(packChunk(x >> 4, z >> 4));
    data.remove(originalBiomeKey());

    refreshChunks(world, chunks);
    return RESTORE_OK;
  }

  public static int paintBiome(Location center, int radiusBlocks, Biome biome) {
    return paintBiome(center, radiusBlocks, biome, false);
  }

  public static int paintBiome(Location center, int radiusBlocks, Biome biome, boolean overwriteOtherLiquids) {
    if (center == null || biome == null) return 0;

    World world = center.getWorld();
    if (world == null) return 0;

    int radius = Math.max(0, Math.min(radiusBlocks,
        NexoAddon.getInstance().getGlobalConfig().getInt("liquid.max_place_radius", 4)));

    int centerX = center.getBlockX();
    int centerY = center.getBlockY();
    int centerZ = center.getBlockZ();

    NamespacedKey target = biome.getKey();
    Set<Long> cells = new HashSet<>();
    Set<Long> chunks = new HashSet<>();
    int blockedByOtherLiquid = 0;

    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -radius; dy <= radius; dy++) {
        for (int dz = -radius; dz <= radius; dz++) {
          int x = centerX + dx;
          int y = centerY + dy;
          int z = centerZ + dz;

          if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;
          if (!cells.add(packCell(x >> 2, y >> 2, z >> 2))) continue;
          if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

          Biome current = world.getBiome(x, y, z);
          if (target.equals(current.getKey())) continue;

          if (!overwriteOtherLiquids && byBiome(current) != null
              && cellHasSource(world, x, y, z, centerX, centerY, centerZ)) {
            blockedByOtherLiquid++;
            continue;
          }

          rememberOriginal(world, x, y, z, current);
          world.setBiome(x, y, z, biome);
          track(world, x, y, z);
          chunks.add(packChunk(x >> 4, z >> 4));
        }
      }
    }

    if (!chunks.isEmpty()) refreshChunks(world, chunks);
    return blockedByOtherLiquid;
  }

  private static void refreshChunks(World world, Set<Long> chunkKeys) {
    if (!NexoAddon.getInstance().getGlobalConfig().getBoolean("liquid.refresh_chunks", true)) return;

    long now = NexoAddon.getInstance().getServer().getCurrentTick();
    long cooldown = Math.max(0L,
        NexoAddon.getInstance().getGlobalConfig().getLong("liquid.refresh_cooldown_ticks", 20));
    prune(now, cooldown);

    for (long chunk : chunkKeys) {
      ChunkRef ref = new ChunkRef(world.getUID(), chunk);
      Long last = lastRefresh.get(ref);
      long wait = last == null ? 0L : cooldown - (now - last);

      if (wait <= 0L) {
        lastRefresh.put(ref, now);
        dispatchRefresh(world, ref, 0L);
        continue;
      }

      if (pendingRefresh.add(ref)) dispatchRefresh(world, ref, wait);
    }
  }

  private static void dispatchRefresh(World world, ChunkRef ref, long delayTicks) {
    int chunkX = (int) (ref.chunk() >> 32);
    int chunkZ = (int) ref.chunk();
    Location location = new Location(world, (chunkX << 4) + 8, world.getMinHeight() + 1, (chunkZ << 4) + 8);

    if (delayTicks <= 0L) {
      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocation(
          location, task -> world.refreshChunk(chunkX, chunkZ));
      return;
    }

    NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(location, task -> {
      pendingRefresh.remove(ref);
      lastRefresh.put(ref, (long) NexoAddon.getInstance().getServer().getCurrentTick());
      world.refreshChunk(chunkX, chunkZ);
    }, delayTicks);
  }

  private static void prune(long now, long cooldown) {
    if (lastRefresh.size() < 512) return;
    lastRefresh.entrySet().removeIf(entry ->
        now - entry.getValue() >= cooldown && !pendingRefresh.contains(entry.getKey()));
  }

  private static long packCell(int cellX, int cellY, int cellZ) {
    return ((long) cellX & 0x3FFFFF) << 42 | ((long) cellY & 0xFFFFF) << 22 | ((long) cellZ & 0x3FFFFF);
  }

  private static long packChunk(int chunkX, int chunkZ) {
    return (long) chunkX << 32 | (chunkZ & 0xFFFFFFFFL);
  }
}
