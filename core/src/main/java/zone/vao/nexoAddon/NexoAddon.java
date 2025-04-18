package zone.vao.nexoAddon;

import co.aikar.commands.PaperCommandManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.nexomc.nexo.api.NexoBlocks;
import io.th0rgal.protectionlib.ProtectionLib;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.commands.NexoAddonCommand;
import zone.vao.nexoAddon.events.PlayerCommandPreprocessListener;
import zone.vao.nexoAddon.events.PrepareRecipesListener;
import zone.vao.nexoAddon.events.WorldLoadListener;
import zone.vao.nexoAddon.events.blocks.BlockBreakListener;
import zone.vao.nexoAddon.events.chunk.ChunkLoadListener;
import zone.vao.nexoAddon.events.nexo.NexoItemsLoadedListener;
import zone.vao.nexoAddon.events.nexo.NexoPackUploadListener;
import zone.vao.nexoAddon.events.nexo.furnitures.NexoFurnitureBreakListener;
import zone.vao.nexoAddon.events.nexo.furnitures.NexoFurnitureInteractListener;
import zone.vao.nexoAddon.events.player.PlayerMovementListener;
import zone.vao.nexoAddon.utils.handlers.BlockHardnessHandler;
import zone.vao.nexoAddon.utils.handlers.ParticleEffectManager;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;
import zone.vao.nexoAddon.utils.hooks.PacketEventsHook;
import zone.vao.nexoAddon.utils.metrics.Metrics;
import zone.vao.nexoAddon.populators.CustomChunkGenerator;
import zone.vao.nexoAddon.populators.orePopulator.CustomOrePopulator;
import zone.vao.nexoAddon.populators.orePopulator.Ore;
import zone.vao.nexoAddon.populators.orePopulator.OrePopulator;
import zone.vao.nexoAddon.populators.treePopulator.CustomTree;
import zone.vao.nexoAddon.populators.treePopulator.CustomTreePopulator;
import zone.vao.nexoAddon.populators.treePopulator.TreePopulator;
import zone.vao.nexoAddon.utils.*;

import java.io.File;
import java.util.*;

@Getter
public final class NexoAddon extends JavaPlugin {

  @Getter
  public static NexoAddon instance;
  public Set<File> nexoFiles = new HashSet<>();
  public Map<String, Components> components = new HashMap<>();
  public Map<String, Mechanics> mechanics = new HashMap<>();
  private Map<String, ItemStack> skulls = new HashMap<>();
  public Map<UUID, BossBarUtil> bossBars = new HashMap<>();
  public FileConfiguration globalConfig;
  public PopulatorsConfigUtil populatorsConfig;
  public List<Ore> ores = new ArrayList<>();
  public List<CustomTree> trees = new ArrayList<>();
  public final OrePopulator orePopulator = new OrePopulator();
  public final TreePopulator treePopulator = new TreePopulator();
  public Map<String, List<BlockPopulator>> worldPopulators = new HashMap<>();
  public Map<String, String> jukeboxLocations = new HashMap<>();
  public Map<String, Integer> customBlockLights = new HashMap<>();
  public BlockHardnessHandler blockHardnessHandler;
  public PacketListenerCommon packetListenerCommon;
  private boolean packeteventsLoaded = false;
  private boolean mythicMobsLoaded = false;
  private ParticleEffectManager particleEffectManager;
  private final Map<Location, BukkitTask> particleTasks = new HashMap<>();


  @Override
  public void onLoad() {
    instance = this;
    if (isPacketEventsPresent()) {
      packeteventsLoaded = true;
      PacketEventsHook.registerListener();
    }else{
      getLogger().warning("");
      getLogger().warning("PacketEvents not found. Some features remain disabled!");
      getLogger().warning("");
    }
  }

  @Override
  public void onEnable() {

    ProtectionLib.init(this);
    saveDefaultConfig();
    globalConfig = getConfig();
    initializeCommandManager();
    if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null &&
        Bukkit.getPluginManager().getPlugin("MythicMobs").isEnabled())
    {
      mythicMobsLoaded = true;
    }

    registerEvents();
    particleEffectManager = new ParticleEffectManager();
    particleEffectManager.startAuraEffectTask();
    initializeMetrics();
    getLogger().info("NexoAddon enabled!");
  }

  @Override
  public void onDisable() {
    bossBars.values().forEach(BossBarUtil::removeBar);
    clearPopulators();
    if(packeteventsLoaded)
      PacketEvents.getAPI().getEventManager().unregisterListener(packetListenerCommon);
    RecipeManager.clearRegisteredRecipes();
    for (Location shiftblock : BlockUtil.processedShiftblocks) {
      PersistentDataContainer pdc = new CustomBlockData(shiftblock.getBlock(), this);
      String targetBlock =  pdc.get(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"), PersistentDataType.STRING);
      if(targetBlock == null || NexoBlocks.blockData(targetBlock) == null) continue;

      shiftblock.getBlock().setBlockData(NexoBlocks.blockData(targetBlock));

      pdc.remove(new NamespacedKey(NexoAddon.getInstance(), "shiftblock_target"));
    }
    particleTasks.values().forEach(BukkitTask::cancel);
    particleTasks.clear();
  }

  @Override
  public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
    return new CustomChunkGenerator(orePopulator);
  }

  public void reload() {
    reloadConfig();
    globalConfig = getConfig();
    Bukkit.getScheduler().runTask(this, () -> {
      clearPopulators();
      initializePopulators();
    });
    reloadNexoFiles();
    loadComponentsIfSupported();
    bossBars.values().forEach(BossBarUtil::removeBar);
    RecipeManager.clearRegisteredRecipes();
    RecipesUtil.loadRecipes();
    SkullUtil.applyTextures();
    particleEffectManager.stopAuraEffectTask();
    new BukkitRunnable() {
      @Override
      public void run(){
        particleEffectManager.startAuraEffectTask();
      }
    }.runTaskLater(this, 2L);
    for (World world : Bukkit.getWorlds()) {
      for (Chunk chunk : world.getLoadedChunks()) {
        BlockUtil.restartBlockAura(chunk);
      }
    }
  }

  private void initializeCommandManager() {
    PaperCommandManager manager = new PaperCommandManager(this);
    manager.registerCommand(new NexoAddonCommand());
  }

  public void initializePopulators() {
    populatorsConfig = new PopulatorsConfigUtil(getDataFolder(), getClassLoader());
    initializeOres();
    initializeTrees();
  }

  private void initializeOres() {
    Bukkit.getScheduler().runTask(this, () -> {
      ores = populatorsConfig.loadOresFromConfig();
      orePopulator.clearOres();
      ores.forEach(orePopulator::addOre);
      Set<World> worldsToAddPopulator = new HashSet<>();
      orePopulator.getOres().forEach(ore -> {
        if(ore.getNexoFurniture() != null) return;
        worldsToAddPopulator.addAll(ore.getWorlds());
      });

      worldsToAddPopulator.forEach(world -> {
        CustomOrePopulator customOrePopulator = new CustomOrePopulator(orePopulator);
        addPopulatorToWorld(world, customOrePopulator);
        worldPopulators.computeIfAbsent(world.getName(), k -> new ArrayList<>()).add(customOrePopulator);
        logPopulatorAdded("BlockPopulator", "all_ores", world);
      });
    });
  }

  private void initializeTrees() {
    trees = populatorsConfig.loadTreesFromConfig();
    treePopulator.clearTrees();
    trees.forEach(treePopulator::addTree);
    treePopulator.getTrees().forEach(tree -> {
      for (World world : tree.getWorlds()) {
        CustomTreePopulator customTreePopulator = new CustomTreePopulator(treePopulator);
        if(!worldPopulators.containsKey(world.getName())) {
          worldPopulators.put(world.getName(), new ArrayList<>());
        }
        addPopulatorToWorld(world, customTreePopulator);
        worldPopulators.get(world.getName()).add(customTreePopulator);
        logPopulatorAdded("TreePopulator", tree.getId(), world);
      }
    });
  }

  private void registerEvents() {
    registerEvent(new NexoItemsLoadedListener());
    registerEvent(new PlayerMovementListener());
    registerEvent(new NexoFurnitureBreakListener());
    registerEvent(new BlockBreakListener());
    registerEvent(new NexoFurnitureInteractListener());
    registerEvent(new ChunkLoadListener());
    registerEvent(new PrepareRecipesListener());
    registerEvent(new PlayerCommandPreprocessListener());
    registerEvent(new WorldLoadListener());
    registerEvent(new NexoPackUploadListener());

    Mechanics.registerListeners(this);
    Components.registerListeners(this);
  }

  private void initializeMetrics() {

    Metrics metrics = new Metrics(this, 24168);
    metrics.addCustomChart(new Metrics.SimplePie("marketplace", () -> "%%__POLYMART__%%".equals("1") ? "polymart" : "spigot"));

    if(getGlobalConfig().getBoolean("update_checker", true))
      new UpdateChecker(this, UpdateCheckSource.POLYMART, "6950")
          .setDownloadLink(6950)
          .checkEveryXHours(24)
          .setNotifyOpsOnJoin(true)
          .setDonationLink("https://buymeacoffee.com/naimad")
          .checkNow();
  }

  private void reloadNexoFiles() {
    nexoFiles.clear();
    nexoFiles.addAll(ItemConfigUtil.getItemFiles());
  }

  private void loadComponentsIfSupported() {
    ItemConfigUtil.loadComponents();
    ItemConfigUtil.loadMechanics();
  }

  private void clearPopulators() {
    worldPopulators.forEach((worldName, populators) -> {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
        getLogger().warning("World '" + worldName + "' not found. Skipping populator removal.");
        return;
      }
      populators.forEach(populator -> {
        if (world.getPopulators().remove(populator)) {
          getLogger().info("Populator removed from world: " + worldName);
        }
      });
    });
    worldPopulators.clear();
  }

  public void removePopulators(World world) {
    worldPopulators.forEach((worldName, populators) -> {
      if(!world.getName().equals(worldName)) return;
      populators.forEach(populator -> {
        if (world.getPopulators().remove(populator)) {
          getLogger().info("Populator removed from world: " + worldName);
        }
      });
    });
    worldPopulators.remove(world.getName());
  }

  public boolean isPacketEventsPresent() {
    try {
      Class.forName("com.github.retrooper.packetevents.event.PacketListener");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void addPopulatorToWorld(World world, BlockPopulator populator) {
    if (world == null) {
      getLogger().severe("World is null. Cannot add Populator.");
      return;
    }
    if (!world.getPopulators().contains(populator)) {
      world.getPopulators().add(populator);
    }
  }

  public void logPopulatorAdded(String type, String name, World world) {
    getLogger().info(type + " of "+name+" added to world: " + world.getName());
  }

  private void registerEvent(Listener listener) {
    getServer().getPluginManager().registerEvents(listener, this);
  }
}
