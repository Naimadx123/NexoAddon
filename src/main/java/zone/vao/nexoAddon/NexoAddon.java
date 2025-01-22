package zone.vao.nexoAddon;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import io.th0rgal.protectionlib.ProtectionLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import zone.vao.nexoAddon.classes.Components;
import zone.vao.nexoAddon.classes.Mechanics;
import zone.vao.nexoAddon.classes.populators.CustomChunkGenerator;
import zone.vao.nexoAddon.classes.populators.orePopulator.CustomOrePopulator;
import zone.vao.nexoAddon.classes.populators.orePopulator.Ore;
import zone.vao.nexoAddon.classes.populators.orePopulator.OrePopulator;
import zone.vao.nexoAddon.classes.populators.treePopulator.CustomTree;
import zone.vao.nexoAddon.classes.populators.treePopulator.CustomTreePopulator;
import zone.vao.nexoAddon.classes.populators.treePopulator.TreePopulator;
import zone.vao.nexoAddon.commands.NexoAddonCommand;
import zone.vao.nexoAddon.events.*;
import zone.vao.nexoAddon.handlers.BlockHardnessHandler;
import zone.vao.nexoAddon.handlers.ParticleEffectManager;
import zone.vao.nexoAddon.handlers.RecipeManager;
import zone.vao.nexoAddon.metrics.Metrics;
import zone.vao.nexoAddon.utils.BossBarUtil;
import zone.vao.nexoAddon.utils.ItemConfigUtil;
import zone.vao.nexoAddon.utils.PopulatorsConfigUtil;
import zone.vao.nexoAddon.utils.RecipesUtil;

import java.io.File;
import java.util.*;

@Getter
public final class NexoAddon extends JavaPlugin {

  @Getter
  public static NexoAddon instance;
  public Set<File> nexoFiles = new HashSet<>();
  public Map<String, Components> components = new HashMap<>();
  public Map<String, Mechanics> mechanics = new HashMap<>();
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
  private BlockHardnessHandler blockHardnessHandler;
  private ProtocolManager protocolManager;
  private boolean protocolLibLoaded = false;
  private ParticleEffectManager particleEffectManager;

    @Override
  public void onEnable() {
    
    instance = this;
    ProtectionLib.init(this);
    saveDefaultConfig();
    globalConfig = getConfig();
    initializeCommandManager();
      if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null &&
          Bukkit.getPluginManager().getPlugin("ProtocolLib").isEnabled()) {
        protocolLibLoaded = true;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.blockHardnessHandler = new BlockHardnessHandler();
        this.blockHardnessHandler.registerListener();
      }else{
        getLogger().warning("ProtocolLib not found. Some features remain disabled!");
      }

      initializePopulators();
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
    RecipeManager.clearRegisteredRecipes();
    if(protocolLibLoaded){
      protocolManager.removePacketListeners(this);
    }
  }

  @Override
  public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
    return new CustomChunkGenerator(orePopulator);
  }

  public void reload() {
    reloadConfig();
    globalConfig = getConfig();
    clearPopulators();
    initializePopulators();
    reloadNexoFiles();
    loadComponentsIfSupported();
    bossBars.values().forEach(BossBarUtil::removeBar);
    RecipeManager.clearRegisteredRecipes();
    RecipesUtil.loadRecipes();
    particleEffectManager.stopAuraEffectTask();
    new BukkitRunnable() {
      @Override
      public void run(){
        particleEffectManager.startAuraEffectTask();
      }
    }.runTaskLater(this, 2L);
  }

  private void initializeCommandManager() {
    PaperCommandManager manager = new PaperCommandManager(this);
    manager.registerCommand(new NexoAddonCommand());
  }

  private void initializePopulators() {
    populatorsConfig = new PopulatorsConfigUtil(getDataFolder(), getClassLoader());
    initializeOres();
    initializeTrees();
  }

  private void initializeOres() {
    ores = populatorsConfig.loadOresFromConfig();
    orePopulator.clearOres();
    ores.forEach(orePopulator::addOre);
    orePopulator.getOres().forEach(ore -> {
      if(ore.getNexoFurniture() != null) return;
      for (World world : ore.getWorlds()) {

        CustomOrePopulator customOrePopulator = new CustomOrePopulator(orePopulator);
        if(!worldPopulators.containsKey(world.getName())) {
          worldPopulators.put(world.getName(), new ArrayList<>());
        }
        addPopulatorToWorld(world, customOrePopulator);
        worldPopulators.get(world.getName()).add(customOrePopulator);
        logPopulatorAdded("BlockPopulator", ore.getId(), world);
      }
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
    registerEvent(new PlayerInteractListener());
    registerEvent(new PlayerMovementListener());
    registerEvent(new NexoFurnitureBreakListener());
    registerEvent(new BlockBreakListener());
    registerEvent(new BlockPlaceListener());
    registerEvent(new NexoFurnitureInteractListener());
    registerEvent(new ChunkLoadListener());
    registerEvent(new InventoryClickListener());
    registerEvent(new PrepareRecipesListener());
    registerEvent(new PlayerCommandPreprocessListener());
    registerEvent(new WorldLoadListener());
    registerEvent(new NexoPackUploadListener());
    registerEvent(new NexoBlockBreakListener());
  }

  private void initializeMetrics() {

    Metrics metrics = new Metrics(this, 24168);
    metrics.addCustomChart(new Metrics.SimplePie("marketplace", () -> "%%__POLYMART__%%".equals("1") ? "polymart" : "spigot"));
    new UpdateChecker(this, UpdateCheckSource.POLYMART, "6950")
        .checkEveryXHours(24)
        .setNotifyOpsOnJoin(true)
        .setDonationLink("https://buymeacoffee.com/naimad")
        .checkNow();
    new UpdateChecker(this, UpdateCheckSource.SPIGET, "121241")
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
