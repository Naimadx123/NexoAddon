package zone.vao.nexoAddon.populators;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import zone.vao.nexoAddon.utils.PopulatorsConfigUtil;

import java.util.List;
import java.util.Random;

public class PopulatorManager {

  private final Plugin plugin;
  private final PopulationEngine engine = new PopulationEngine();
  private final NexoPopulator populator = new NexoPopulator(this);
  private volatile Settings settings = new Settings(List.of(), List.of(), true);

  public PopulatorManager(Plugin plugin) {
    this.plugin = plugin;
  }

  public void reload(PopulatorsConfigUtil config) {
    settings = new Settings(config.loadBlockPopulators(), config.loadBiomePopulatorsFromConfig(), plugin.getConfig().getBoolean("populators.enabled", true));
    Bukkit.getWorlds().forEach(this::attach);
  }

  public NexoPopulator getPopulator() {
    return populator;
  }

  public boolean appliesToWorld(String name) {
    Settings current = settings;
    return current.blocks().stream().anyMatch(rule -> rule.appliesToWorld(name))
        || current.biomes().stream().anyMatch(rule -> rule.appliesToWorld(name));
  }

  public void attach(World world) {
    if (!settings.populatorsEnabled() || world.getGenerator() instanceof CustomChunkGenerator) {
      detach(world);
      return;
    }
    if (!world.getPopulators().contains(populator)) world.getPopulators().add(populator);
  }

  public void detach(World world) {
    world.getPopulators().remove(populator);
  }

  public void clear() {
    settings = new Settings(List.of(), List.of(), false);
    Bukkit.getWorlds().forEach(this::detach);
  }

  public void populate(WorldInfo world, Random random, int chunkX, int chunkZ, LimitedRegion region) {
    Settings current = settings;
    if (!current.populatorsEnabled()) return;
    PopulationRegion view = new LimitedPopulationRegion(region);
    current.biomes().forEach(biome -> biome.populate(world, random, chunkX, chunkZ, view));
    for (PopulatorDefinition rule : current.blocks()) {
      if (!rule.item().requiresWorld()) engine.populate(world, random, chunkX, chunkZ, view, rule);
    }
  }

  public void generate(WorldInfo world, int chunkX, int chunkZ, ChunkData data) {
    Settings current = settings;
    Random random = random(world, chunkX, chunkZ);
    PopulationRegion view = new ChunkPopulationRegion(data, chunkX, chunkZ);
    current.biomes().forEach(biome -> biome.populate(world, random, chunkX, chunkZ, view));
    for (PopulatorDefinition rule : current.blocks()) {
      if (!rule.requiresDecoration()) engine.populate(world, random, chunkX, chunkZ, view, rule);
    }
  }

  public void populateDeferred(Chunk chunk) {
    World world = chunk.getWorld();
    int chunkX = chunk.getX();
    int chunkZ = chunk.getZ();
    Settings current = settings;
    boolean generator = world.getGenerator() instanceof CustomChunkGenerator;
    if (!generator && !current.populatorsEnabled()) return;
    List<PopulatorDefinition> rules = current.blocks().stream()
        .filter(rule -> (generator ? rule.requiresDecoration() : rule.item().requiresWorld()) && rule.appliesToWorld(world.getName()))
        .toList();
    List<BiomePopulator> biomes = generator ? current.biomes().stream().filter(rule -> rule.appliesToWorld(world.getName())).toList() : List.of();
    if (rules.isEmpty() && biomes.isEmpty()) return;
    Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> {
      if (settings != current || !world.isChunkLoaded(chunkX, chunkZ)) return;
      Random random = random(world, chunkX, chunkZ);
      PopulationRegion view = new WorldPopulationRegion(world, chunkX, chunkZ);
      boolean changed = false;
      for (BiomePopulator biome : biomes) changed |= biome.populate(world, random, chunkX, chunkZ, view);
      for (PopulatorDefinition rule : rules) engine.populate(world, random, chunkX, chunkZ, view, rule);
      if (changed) world.refreshChunk(chunkX, chunkZ);
    });
  }

  public void repopulate(World world, Chunk chunk) {
    int chunkX = chunk.getX();
    int chunkZ = chunk.getZ();
    Settings current = settings;
    Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> {
      if (settings != current || !world.isChunkLoaded(chunkX, chunkZ)) return;
      Random random = random(world, chunkX, chunkZ);
      PopulationRegion view = new WorldPopulationRegion(world, chunkX, chunkZ);
      boolean changed = false;
      for (BiomePopulator biome : current.biomes()) changed |= biome.populate(world, random, chunkX, chunkZ, view);
      for (PopulatorDefinition rule : current.blocks()) engine.populate(world, random, chunkX, chunkZ, view, rule);
      if (changed) world.refreshChunk(chunkX, chunkZ);
    });
  }

  private Random random(WorldInfo world, int chunkX, int chunkZ) {
    return new Random(world.getSeed() ^ chunkX * 341873128712L ^ chunkZ * 132897987541L);
  }

  private record Settings(List<PopulatorDefinition> blocks, List<BiomePopulator> biomes, boolean populatorsEnabled) {

    private Settings {
      blocks = List.copyOf(blocks);
      biomes = List.copyOf(biomes);
    }
  }
}
