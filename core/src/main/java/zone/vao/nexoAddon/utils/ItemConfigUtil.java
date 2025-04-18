package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.items.Mechanics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemConfigUtil {

  private static final Set<File> itemFiles = new HashSet<>();

  public static Set<File> getItemFiles() {
    itemFiles.clear();
    itemFiles.addAll(NexoItems.itemMap().keySet());
    return itemFiles;
  }

  public static void loadComponents() {
    NexoAddon.getInstance().getComponents().clear();

    for (File itemFile : getItemFiles()) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);

      config.getKeys(false).forEach(itemId -> {
        ConfigurationSection itemSection = config.getConfigurationSection(itemId);
        if (itemSection == null || !itemSection.contains("Components")) return;

        Components component = NexoAddon.getInstance().getComponents()
                .computeIfAbsent(itemId, Components::new);

        loadEquippableComponent(itemSection, component);
        loadJukeboxPlayableComponent(itemSection, component);
        loadFertilizerComponent(itemSection, component);
        loadSkullValueComponent(itemSection, component);
      });
    }
  }

  private static void loadEquippableComponent(ConfigurationSection section, Components component) {
    if (section.contains("Components.equippable")) {
      try {
        EquipmentSlot slot = EquipmentSlot.valueOf(
                section.getString("Components.equippable.slot", "HEAD").toUpperCase()
        );
        component.setEquippable(slot);
      } catch (IllegalArgumentException ignored) {}
    }
  }

  private static void loadJukeboxPlayableComponent(ConfigurationSection section, Components component) {
    if (section.contains("Components.jukebox_playable.song_key")) {
      String songKey = section.getString("Components.jukebox_playable.song_key");
      component.setPlayable(songKey);
    }
  }

  private static void loadFertilizerComponent(ConfigurationSection section, Components component) {
    if (section.contains("Components.fertilizer.growth_speedup") && section.contains("Components.fertilizer.usable_on")) {
      int growthSpeedup = section.getInt("Components.fertilizer.growth_speedup", 1000);
      List<String> usableOn = section.getStringList("Components.fertilizer.usable_on");
      component.setFertilizer(growthSpeedup, usableOn, section.getInt("Components.fertilizer.cooldown", 0));
    }
  }

  private static void loadSkullValueComponent(ConfigurationSection section, Components component) {
    if (section.contains("Components.skull_value")) {
      String value = section.getString("Components.skull_value", SkullUtil.NEXOADDON_HEAD_BASE64);
      component.setSkullValue(value);
    }
  }


  public static void loadMechanics() {
    NexoAddon.getInstance().getMechanics().clear();

    for (File itemFile : getItemFiles()) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);

      config.getKeys(false).forEach(itemId -> {
        ConfigurationSection itemSection = config.getConfigurationSection(itemId);
        if (itemSection == null || !itemSection.contains("Mechanics")) return;

        Mechanics mechanic = NexoAddon.getInstance().getMechanics()
                .computeIfAbsent(itemId, Mechanics::new);

        loadRepairMechanic(itemSection, mechanic);
        loadBigMiningMechanic(itemSection, mechanic);
        loadBedrockBreakMechanic(itemSection, mechanic);
        loadAuraMechanic(itemSection, mechanic);
        loadSpawnerBreak(itemSection, mechanic);
        loadMiningToolsMechanic(itemSection, mechanic);
        loadDropExperienceMechanic(itemSection, mechanic);
        loadInfested(itemSection, mechanic);
        loadKillMessage(itemSection, mechanic);
        loadStackableStringblockMechanic(itemSection, mechanic);
        loadDecayMechanic(itemSection, mechanic);
        loadShiftBlockMechanic(itemSection, mechanic);
        loadBottledExpMechanic(itemSection, mechanic);
        loadUnstackableStringblockMechanic(itemSection, mechanic);
        loadBlockAuraMechanic(itemSection, mechanic);
        loadSignalMechanic(itemSection, mechanic);
        loadRememberMechanic(itemSection, mechanic);
      });
    }
  }

  private static void loadRepairMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.repair.ratio") || section.contains("Mechanics.repair.fixed_amount")) {
      double ratio = section.getDouble("Mechanics.repair.ratio");
      int fixedAmount = section.getInt("Mechanics.repair.fixed_amount");
      List<String> rawItems = section.getStringList("Mechanics.repair.whitelist");
      List<String> rawItemsBlacklist = section.getStringList("Mechanics.repair.blacklist");
      List<Material> materials = new ArrayList<>();
      List<String> nexoIds = new ArrayList<>();
      List<Material> materialsBlacklist = new ArrayList<>();
      List<String> nexoIdsBlacklist = new ArrayList<>();
      if(!rawItems.isEmpty()){
        for (String rawItem : rawItems) {
          if(Material.matchMaterial(rawItem) != null) {
            materials.add(Material.matchMaterial(rawItem));
            continue;
          }
          if(NexoItems.itemFromId(rawItem) != null) {
            nexoIds.add(rawItem);
          }
        }
      }
      if(!rawItemsBlacklist.isEmpty()){
        for (String rawItem : rawItemsBlacklist) {
          if(Material.matchMaterial(rawItem) != null) {
            materialsBlacklist.add(Material.matchMaterial(rawItem));
            continue;
          }
          if(NexoItems.itemFromId(rawItem) != null) {
            nexoIdsBlacklist.add(rawItem);
          }
        }
      }

      mechanic.setRepair(ratio, fixedAmount, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
    }
  }

  private static void loadBigMiningMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.bigmining.radius") && section.contains("Mechanics.bigmining.depth")) {
      int radius = section.getInt("Mechanics.bigmining.radius", 1);
      int depth = section.getInt("Mechanics.bigmining.depth", 1);
      boolean switchable = section.getBoolean("Mechanics.bigmining.switchable", false);
      List<Material> materials = new ArrayList<>();
      for (String s : section.getStringList("Mechanics.bigmining.materials")) {
        Material material = Material.matchMaterial(s);
        if(material != null) materials.add(material);
      }

      mechanic.setBigMining(radius, depth, switchable, materials);
    }
  }

  private static void loadBedrockBreakMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.bedrockbreak.hardness") && section.contains("Mechanics.bedrockbreak.probability")) {
      int hardness = section.getInt("Mechanics.bedrockbreak.hardness");
      double probability = section.getDouble("Mechanics.bedrockbreak.probability");
      int durabilityCost = section.getInt("Mechanics.bedrockbreak.durability_cost", 1);
      boolean disableOnFirstLayer = section.getBoolean("Mechanics.bedrockbreak.disable_on_first_layer", true);
      mechanic.setBedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer);
    }
  }

  private static void loadAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.aura.type") && section.contains("Mechanics.aura.particle")) {
      Particle particle = Particle.valueOf(section.getString("Mechanics.aura.particle", "FLAME").toUpperCase());
      String type = section.getString("Mechanics.aura.type");
      String customFormula = section.getString("Mechanics.aura.custom", null);
      mechanic.setAura(particle, type, customFormula);
    }
  }

  private static void loadSpawnerBreak(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.spawnerbreak.probability")) {
      double probability = section.getDouble("Mechanics.spawnerbreak.probability");
      boolean dropExperience = section.getBoolean("Mechanics.spawnerbreak.dropExperience", false);
      mechanic.setSpawnerBreak(probability, dropExperience);
    }
  }
  
  private static void loadMiningToolsMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.miningtools.items")) {
      List<String> values = section.getStringList("Mechanics.custom_block.miningtools.items");
      List<Material> materials = new ArrayList<>();
      List<String> nexoIds = new ArrayList<>();

      for (String value : values) {
        Material material = Material.matchMaterial(value);
        if(material != null) materials.add(material);
        if(NexoItems.itemFromId(value) != null) nexoIds.add(value);
      }

      mechanic.setMiningTools(materials, nexoIds, section.getString("Mechanics.custom_block.miningtools.type", "CANCEL_EVENT"));
    }
  }

  private static void loadDropExperienceMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.drop.experience")) {
      double experience = section.getDouble("Mechanics.custom_block.drop.experience", 0.0);
      mechanic.setDropExperience(experience);
    }
  }

  private static void loadInfested(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.infested.entities") || section.contains("Mechanics.custom_block.infested.mythic-mobs")) {
      List<String> values = section.getStringList("Mechanics.custom_block.infested.entities");
      List<EntityType> entities = new ArrayList<>();
      for (String value : values) {
        try {
          EntityType entityType = EntityType.valueOf(value.toUpperCase());
          entities.add(entityType);
        } catch (IllegalArgumentException e) {
          NexoAddon.getInstance().getLogger().info("Invalid EntityType: " + value);
        }
      }

      List<String> mythicMobs = List.of();
      if(NexoAddon.getInstance().isMythicMobsLoaded() && section.contains("Mechanics.custom_block.infested.mythic-mobs")) {
        mythicMobs = section.getStringList("Mechanics.custom_block.infested.mythic-mobs");
      } else if(!NexoAddon.getInstance().isMythicMobsLoaded() && section.contains("Mechanics.custom_block.infested.mythic-mobs")){
        NexoAddon.getInstance().getLogger().warning("MythicMobs is not loaded! Skipping `infested.mythic-mobs`");
      }

      double probability = section.getDouble("Mechanics.custom_block.infested.probability", 1.0);
      String selector = section.getString("Mechanics.custom_block.infested.selector", "all");
      boolean particles = section.getBoolean("Mechanics.custom_block.infested.particles", false);
      boolean drop = section.getBoolean("Mechanics.custom_block.infested.drop-loot", true);

      mechanic.setInfested(entities, mythicMobs, probability, selector, particles, drop);
    }
  }
  
  private static void loadKillMessage(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.kill_message")) {
      String deathMessage = section.getString("Mechanics.kill_message", null);
      mechanic.setKillMessage(deathMessage);
    }
  }
  
  private static void loadStackableStringblockMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.stackable.next")
        && section.contains("Mechanics.custom_block.stackable.group")
    ) {
      mechanic.setStackable(section.getString("Mechanics.custom_block.stackable.next"), section.getString("Mechanics.custom_block.stackable.group"));
    }
  }

  private static void loadDecayMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.decay.base")
        && section.contains("Mechanics.custom_block.decay.time")
        && section.contains("Mechanics.custom_block.decay.chance")
        && section.contains("Mechanics.custom_block.decay.radius")
    ) {
      int time = section.getInt("Mechanics.custom_block.decay.time", 5);
      double chance = section.getDouble("Mechanics.custom_block.decay.chance", 0.3);
      List<String> base = section.getStringList("Mechanics.custom_block.decay.base");
      int radius = section.getInt("Mechanics.custom_block.decay.radius", 5);

      List<String> nexoBaseFinal = new ArrayList<>();
      List<Material> baseFinal = new ArrayList<>();
      for (String s : base) {
        if(NexoItems.itemFromId(s) != null){
          nexoBaseFinal.add(s);
        }
        else if(Material.matchMaterial(s) != null){
          baseFinal.add(Material.matchMaterial(s));
        }
      }
      mechanic.setDecay(time, chance, baseFinal, nexoBaseFinal, radius);
    }
  }

  private static void loadShiftBlockMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.shiftblock.time")
        && section.contains("Mechanics.shiftblock.replace_to")
    ) {
      List<Material> materials = new ArrayList<>();
      List<String> nexoIds = new ArrayList<>();
      if(section.contains("Mechanics.shiftblock.items")){
        for (String s : section.getStringList("Mechanics.shiftblock.items")) {
          if(NexoItems.itemFromId(s) != null){
            nexoIds.add(s);
          }else if(Material.matchMaterial(s) != null){
            materials.add(Material.matchMaterial(s));
          }
        }
      }

      mechanic.setShiftBlock(section.getString("Mechanics.shiftblock.replace_to"), section.getInt("Mechanics.shiftblock.time",200), materials, nexoIds, section.getBoolean("Mechanics.shiftblock.on_interact",true), section.getBoolean("Mechanics.shiftblock.on_break",false), section.getBoolean("Mechanics.shiftblock.on_place",false));
    }
  }

  private static void loadBottledExpMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.bottledexp.ratio")) {
      mechanic.setBottledExp(section.getDouble("Mechanics.bottledexp.ratio", 0.5), section.getInt("Mechanics.bottledexp.cost", 1));
    }
  }

  private static void loadUnstackableStringblockMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.unstackable.next")
        && section.contains("Mechanics.custom_block.unstackable.give")
    ) {
      List<String> rawItems = section.getStringList("Mechanics.custom_block.unstackable.items");
      List<Material> materials = new ArrayList<>();
      List<String> nexoIds = new ArrayList<>();

      for (String rawItem : rawItems) {
        if(Material.matchMaterial(rawItem) != null){
          materials.add(Material.matchMaterial(rawItem));
          continue;
        }
        if(NexoItems.itemFromId(rawItem) != null){
          nexoIds.add(rawItem);
        }
      }

      mechanic.setUnstackable(section.getString("Mechanics.custom_block.unstackable.next"), section.getString("Mechanics.custom_block.unstackable.give"), materials, nexoIds);
    }
  }

  private static void loadBlockAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.custom_block.block_aura.particle")) {
      Particle particle = Particle.valueOf(section.getString("Mechanics.custom_block.block_aura.particle", "FLAME").toUpperCase());
      String xOffset = section.getString("Mechanics.custom_block.block_aura.xOffset", "0.5");
      String yOffset = section.getString("Mechanics.custom_block.block_aura.yOffset", "0.5");
      String zOffset = section.getString("Mechanics.custom_block.block_aura.zOffset", "0.5");
      int amount = section.getInt("Mechanics.custom_block.block_aura.amount", 10);
      double deltaX = section.getDouble("Mechanics.custom_block.block_aura.deltaX", 0.6);
      double deltaY = section.getDouble("Mechanics.custom_block.block_aura.deltaY", 0.6);
      double deltaZ = section.getDouble("Mechanics.custom_block.block_aura.deltaZ", 0.6);
      double speed = section.getDouble("Mechanics.custom_block.block_aura.speed", 0.05);
      boolean force = section.getBoolean("Mechanics.custom_block.block_aura.force", true);
      mechanic.setBlockAura(particle, xOffset, yOffset, zOffset, amount, deltaX, deltaY, deltaZ, speed, force);
    }
  }

  private static void loadSignalMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.furniture.signal.role")
        && section.contains("Mechanics.furniture.signal.channel")
    ) {
      int radius = section.getInt("Mechanics.furniture.signal.radius", 16);
      double channel = section.getDouble("Mechanics.furniture.signal.channel");
      String role = section.getString("Mechanics.furniture.signal.role");
      mechanic.setSignal(radius, channel, role);
    }
  }

  private static void loadRememberMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.furniture.remember")) {

      mechanic.setRemember(section.getBoolean("Mechanics.furniture.remember", true));
    }
  }
}
