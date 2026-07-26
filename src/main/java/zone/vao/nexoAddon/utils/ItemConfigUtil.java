package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.biomes.CustomBiomeState;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.Liquid;
import zone.vao.nexoAddon.items.mechanics.Spread;

import java.io.File;
import java.util.*;

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
        loadFertilizerComponent(itemSection, component);
        loadSkullValueComponent(itemSection, component);
        loadNoteBlockSoundComponent(itemSection, component);
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

  private static void loadNoteBlockSoundComponent(ConfigurationSection section, Components component) {
    if (section.contains("Components.note_block_sound")) {
      String soundId = section.getString("Components.note_block_sound");
      component.setNoteBlockSound(soundId);
    }
  }


  public static void loadMechanics() {
    NexoAddon.getInstance().getMechanics().clear();
    LiquidUtil.clear();
    CooldownUtil.clearAll();
    NexoAddon.getInstance().setIsLiquid(false);

    for (File itemFile : getItemFiles()) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(itemFile);

      config.getKeys(false).forEach(itemId -> {
        ConfigurationSection itemSection = config.getConfigurationSection(itemId);
        if (itemSection == null || !itemSection.contains("Mechanics")) return;

        Mechanics mechanic = NexoAddon.getInstance().getMechanics()
                .computeIfAbsent(itemId, Mechanics::new);

        loadRepairMechanic(itemSection, mechanic);
        loadBigMiningMechanic(itemSection, mechanic);
        loadVeinMinerMechanic(itemSection, mechanic);
        loadBedrockBreakMechanic(itemSection, mechanic);
        loadAuraMechanic(itemSection, mechanic);
        loadSpawnerBreak(itemSection, mechanic);
        loadMiningToolsMechanic(itemSection, mechanic);
        loadDropExperienceMechanic(itemSection, mechanic);
        loadInfested(itemSection, mechanic);
        loadKillMessage(itemSection, mechanic);
        loadStackableMechanic(itemSection, mechanic);
        loadDecayMechanic(itemSection, mechanic);
        loadShiftBlockMechanic(itemSection, mechanic);
        loadBottledExpMechanic(itemSection, mechanic);
        loadUnstackableMechanic(itemSection, mechanic);
        loadBlockAuraMechanic(itemSection, mechanic);
        loadSignalMechanic(itemSection, mechanic);
        loadRememberMechanic(itemSection, mechanic);
        loadEnchantifyMechanic(itemSection, mechanic);
        loadAutoCatchMechanic(itemSection, mechanic);
        loadUniqueIdMechanic(itemSection, mechanic);
        loadInventoryType(itemSection, mechanic);
        loadLifesteal(itemSection, mechanic);
        loadSpreadMechanic(itemSection, mechanic);
        loadThorMechanic(itemSection, mechanic);
        loadLiquidMechanic(itemSection, mechanic);
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
      String soundRaw = section.getString("Mechanics.bigmining.sound", "block.stone.break");
      List<Material> materials = new ArrayList<>();
      for (String s : section.getStringList("Mechanics.bigmining.materials")) {
        Material material = Material.matchMaterial(s);
        if(material != null) materials.add(material);
      }

      Sound sound = Registry.SOUNDS.get(soundRaw.contains(":") ? NamespacedKey.fromString(soundRaw) : NamespacedKey.minecraft(soundRaw));

      mechanic.setBigMining(radius, depth, switchable, materials, sound);
    }
  }

  private static void loadVeinMinerMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.veinminer.distance")) {
      int distance = section.getInt("Mechanics.veinminer.distance", 10);
      boolean toggleable = section.getBoolean("Mechanics.veinminer.toggleable", false);
      boolean sameMaterial = section.getBoolean("Mechanics.veinminer.same_material", true);
      int limit = section.getInt("Mechanics.veinminer.limit", 10);

      List<Material> materials = new ArrayList<>();
      List<String> nexoIds = new ArrayList<>();

      for (String s : section.getStringList("Mechanics.veinminer.whitelist")) {
        Material material = Material.matchMaterial(s);
        if (material != null) {
          materials.add(material);
        } else if (NexoItems.itemFromId(s) != null) {
          nexoIds.add(s);
        }
      }

      mechanic.setVeinMiner(distance, toggleable, sameMaterial, limit, materials, nexoIds);
    }
  }

  private static void loadBedrockBreakMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.bedrockbreak.hardness") && section.contains("Mechanics.bedrockbreak.probability")) {
      int hardness = section.getInt("Mechanics.bedrockbreak.hardness");
      double probability = section.getDouble("Mechanics.bedrockbreak.probability");
      int durabilityCost = section.getInt("Mechanics.bedrockbreak.durability_cost", 1);
      boolean disableOnFirstLayer = section.getBoolean("Mechanics.bedrockbreak.disable_on_first_layer", true);
      String soundRaw = section.getString("Mechanics.bedrockbreak.sound", "block.stone.break");
      Sound sound = Registry.SOUNDS.get(soundRaw.contains(":") ? NamespacedKey.fromString(soundRaw) : NamespacedKey.minecraft(soundRaw));
      mechanic.setBedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer, sound);
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
      boolean safeSpawn = section.getBoolean("Mechanics.custom_block.infested.safe-spawn", false);

      mechanic.setInfested(entities, mythicMobs, probability, selector, particles, drop, safeSpawn);
    }
  }

  private static void loadKillMessage(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.kill_message")) {
      String deathMessage = section.getString("Mechanics.kill_message", null);
      mechanic.setKillMessage(deathMessage);
    }
  }

  private static void loadStackableMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.stackable.next")
        && section.contains("Mechanics.stackable.group")
    ) {
      mechanic.setStackable(section.getString("Mechanics.stackable.next"), section.getString("Mechanics.stackable.group"));
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
      NexoAddon.instance.setIsDecay(true);
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
      mechanic.setBottledExp((Double) section.getDouble("Mechanics.bottledexp.ratio", 0.5), section.getInt("Mechanics.bottledexp.cost", 1));
    }
  }

  private static void loadUnstackableMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.unstackable.next")
        && section.contains("Mechanics.unstackable.give")
    ) {
      List<String> rawItems = section.getStringList("Mechanics.unstackable.items");
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

      mechanic.setUnstackable(section.getString("Mechanics.unstackable.next"), section.getString("Mechanics.unstackable.give"), materials, nexoIds);
    }
  }

  private static void loadBlockAuraMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (section.contains("Mechanics.block_aura.particle")) {
      Particle particle = Particle.valueOf(section.getString("Mechanics.block_aura.particle", "FLAME").toUpperCase());
      String xOffset = section.getString("Mechanics.block_aura.xOffset", "0.5");
      String yOffset = section.getString("Mechanics.block_aura.yOffset", "0.5");
      String zOffset = section.getString("Mechanics.block_aura.zOffset", "0.5");
      int amount = section.getInt("Mechanics.block_aura.amount", 10);
      double deltaX = section.getDouble("Mechanics.block_aura.deltaX", 0.6);
      double deltaY = section.getDouble("Mechanics.block_aura.deltaY", 0.6);
      double deltaZ = section.getDouble("Mechanics.block_aura.deltaZ", 0.6);
      double speed = section.getDouble("Mechanics.block_aura.speed", 0.05);
      boolean force = section.getBoolean("Mechanics.block_aura.force", true);
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

  private static void loadEnchantifyMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.enchantify.enchants")) return;

    Map<Enchantment, Integer> enchants = new HashMap<>();
    Map<Enchantment, Integer> limits = new HashMap<>();
    parseEnchantments(section.getMapList("Mechanics.enchantify.enchants"), enchants, limits);

    List<Material> materials = new ArrayList<>();
    List<String> nexoIds = new ArrayList<>();
    parseItemList(section.getStringList("Mechanics.enchantify.whitelist"), materials, nexoIds);

    List<Material> materialsBlacklist = new ArrayList<>();
    List<String> nexoIdsBlacklist = new ArrayList<>();
    parseItemList(section.getStringList("Mechanics.enchantify.blacklist"), materialsBlacklist, nexoIdsBlacklist);

    mechanic.setEnchantify(enchants, limits, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
  }

  private static void loadAutoCatchMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.autocatch")) return;

    mechanic.setAutoCatch(section.getBoolean("Mechanics.autocatch.toggable", false), section.getBoolean("Mechanics.autocatch.recast", true));
  }

    private static void loadUniqueIdMechanic(ConfigurationSection section, Mechanics mechanic) {
        if (!section.contains("Mechanics.uniqueid")) return;

        mechanic.setUniqueId(section.getBoolean("Mechanics.uniqueid.enabled", true));
    }

  private static void parseEnchantments(List<Map<?, ?>> enchantList, Map<Enchantment, Integer> enchants, Map<Enchantment, Integer> limits) {
    for (Map<?, ?> map : enchantList) {
      String enchantName = String.valueOf(map.get("enchant")).toLowerCase();
      if (enchantName == null || enchantName.isEmpty()) continue;

      if (!enchantName.contains(":")) {
        enchantName = "minecraft:" + enchantName;
      }

      int level = (map.get("level") instanceof Number number) ? number.intValue() : 0;
      int limit = (map.get("limit") instanceof Number l) ? l.intValue() : 0;

      Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.fromString(enchantName));
      if (enchantment != null && level > 0) {
        enchants.put(enchantment, Integer.valueOf(level));
        if (limit > 0) {
          limits.put(enchantment, Integer.valueOf(limit));
        }
      }
    }
  }

  private static void loadInventoryType(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.inventoryType")) return;

    InventoryType type = null;
    try {
      type = InventoryType.valueOf(section.getString("Mechanics.inventoryType.type", "WORKBENCH").toUpperCase());
    } catch (IllegalArgumentException e) {
      NexoAddon.getInstance().getLogger().warning("Invalid InventoryType: " + section.getString("Mechanics.inventoryType.type"));
    }

    if (type == null) {
      NexoAddon.getInstance().getLogger().warning("Invalid InventoryType: " + section.getString("Mechanics.inventoryType.type"));
      return;
    }

    String titleRaw = section.getString("Mechanics.inventoryType.title");
    Component title = null;
    if(titleRaw == null || titleRaw.isEmpty()){
      title = type.defaultTitle();
    } else {
      title = MiniMessage.miniMessage().deserialize(titleRaw);
    }

    mechanic.setInventoryType(type, title);
  }

  private static void loadLifesteal(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.lifesteal")) return;

    mechanic.setLifesteal(section.getInt("Mechanics.lifesteal.amount", 1), section.getDouble("Mechanics.lifesteal.cooldown", 0.0));
  }

  private static void loadLiquidMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.liquid")) return;
    if (!section.getBoolean("Mechanics.liquid.enabled", true)) return;

    String itemId = mechanic.getId();
    String biomeKey = section.getString("Mechanics.liquid.biome");
    if (biomeKey == null || biomeKey.isBlank()) {
      NexoAddon.getInstance().getLogger().warning("Liquid mechanic on `" + itemId + "` has no `biome`. Skipping.");
      return;
    }

    Biome biome = LiquidUtil.resolveBiome(biomeKey);
    if (biome == null) {
      NexoAddon.getInstance().getLogger().warning(liquidBiomeError(itemId, biomeKey));
      return;
    }
    if (!biomeKey.contains(":") || biomeKey.startsWith("minecraft:")) {
      NexoAddon.getInstance().getLogger().info("Liquid `" + itemId + "` uses the vanilla biome `" + biomeKey
          + "`; naturally occurring water of that biome will behave as this liquid too.");
    }

    boolean followFlow = section.getBoolean("Mechanics.liquid.follow_flow", true);
    boolean placeEnabled = section.getBoolean("Mechanics.liquid.place.enabled", true);
    boolean overwriteOtherLiquids = section.getBoolean("Mechanics.liquid.place.overwrite_other_liquids", false);
    int placeRadius = Math.max(0, section.getInt("Mechanics.liquid.place.radius", 0));
    String emptyItem = section.getString("Mechanics.liquid.place.empty_item");
    String bucketItem = section.getString("Mechanics.liquid.place.bucket_item");
    String bottleItem = section.getString("Mechanics.liquid.place.bottle_item");

    Biome revertTo = null;
    String revertKey = section.getString("Mechanics.liquid.place.revert_to");
    if (revertKey != null && !revertKey.isBlank()) {
      revertTo = LiquidUtil.resolveBiome(revertKey);
      if (revertTo == null)
        NexoAddon.getInstance().getLogger().warning("Unknown biome `" + revertKey
            + "` in liquid.place.revert_to on `" + itemId + "`. Ignoring it.");
    }

    List<PotionEffect> effects = parsePotionEffects(section.getMapList("Mechanics.liquid.enter.effects.list"), itemId);
    double effectsCooldown = section.getDouble("Mechanics.liquid.enter.effects.cooldown", 0.0);

    List<String> commands = List.copyOf(section.getStringList("Mechanics.liquid.enter.commands.list"));
    double commandsCooldown = section.getDouble("Mechanics.liquid.enter.commands.cooldown", 0.0);
    boolean commandsAsConsole = !"player".equalsIgnoreCase(section.getString("Mechanics.liquid.enter.commands.as", "console"));

    Liquid liquid = new Liquid(
        itemId, biome, biomeKey, followFlow, placeEnabled, overwriteOtherLiquids, placeRadius,
        emptyItem, bucketItem, bottleItem, revertTo,
        effects, effectsCooldown, "liquid_fx:" + biomeKey,
        commands, commandsCooldown, "liquid_cmd:" + biomeKey, commandsAsConsole
    );

    if (!LiquidUtil.register(liquid)) return;

    mechanic.setLiquid(liquid);
    NexoAddon.getInstance().setIsLiquid(true);
  }

  private static String liquidBiomeError(String itemId, String biomeKey) {
    String prefix = "Liquid `" + itemId + "`: biome `" + biomeKey + "` ";

    if (CustomBiomeState.isDefined(biomeKey)) {
      if (!CustomBiomeState.isEnabled(biomeKey)) {
        return prefix + "is defined in custom_biomes/ but is not enabled. Set `enabled: true` on it "
            + "and restart the server. Skipping.";
      }
      return prefix + "is defined in custom_biomes/ but is not registered yet. Restart the server to "
          + "apply it — biome registries can only change at startup. Skipping.";
    }

    if (CustomBiomeState.bootstrapRan()) {
      return prefix + "is unknown. Define it in plugins/NexoAddon/custom_biomes/ and restart, or use a "
          + "vanilla biome. Run /nexoaddon biomes to see what is registered. Skipping.";
    }

    return prefix + "is unknown, and custom biome generation did not run (disabled via config or "
        + "-Dnexoaddon.biomes=off, or it failed at startup — check the log). Skipping.";
  }

  private static List<PotionEffect> parsePotionEffects(List<Map<?, ?>> effectList, String itemId) {
    List<PotionEffect> effects = new ArrayList<>();
    for (Map<?, ?> map : effectList) {
      String typeName = String.valueOf(map.get("type")).toLowerCase();
      if (typeName.isEmpty() || "null".equals(typeName)) continue;

      if (!typeName.contains(":")) {
        typeName = "minecraft:" + typeName;
      }

      PotionEffectType type = null;
      try {
        NamespacedKey key = NamespacedKey.fromString(typeName);
        if (key != null) type = Registry.MOB_EFFECT.get(key);
      } catch (Throwable ignored) {
      }

      if (type == null) {
        NexoAddon.getInstance().getLogger().warning("Invalid potion effect `" + typeName
            + "` in liquid mechanic on `" + itemId + "`. Skipping it.");
        continue;
      }

      int duration = Math.max(1, (map.get("duration") instanceof Number number) ? number.intValue() : 100);
      int amplifier = Math.max(0, (map.get("amplifier") instanceof Number number) ? number.intValue() : 0);
      boolean ambient = (map.get("ambient") instanceof Boolean bool) && bool;
      boolean particles = !(map.get("particles") instanceof Boolean bool) || bool;
      boolean icon = !(map.get("icon") instanceof Boolean bool) || bool;

      effects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
    }
    return List.copyOf(effects);
  }

  private static void loadThorMechanic(ConfigurationSection section, Mechanics mechanic) {
    if (!section.contains("Mechanics.thor")) return;

    int lightningBoltsAmount = Math.max(1, section.getInt("Mechanics.thor.lightning_bolts_amount", 1));
    double randomLocationVariation = Math.max(0.0, section.getDouble("Mechanics.thor.random_location_variation", 1.5));
    int delay = Math.max(0, section.getInt("Mechanics.thor.delay", 0));

    mechanic.setThor(lightningBoltsAmount, randomLocationVariation, delay);
  }

  private static void loadSpreadMechanic(ConfigurationSection section, Mechanics mechanic) {
    ConfigurationSection spread = section.getConfigurationSection("Mechanics.custom_block.spread");
    if (spread == null || !spread.getBoolean("enabled", true)) return;

    String itemId = mechanic.getId();

    int interval = Math.max(1, spread.getInt("interval", 100));
    double chance = spread.getDouble("chance", 0.15);
    int radius = Math.max(1, spread.getInt("radius", 1));
    boolean requiresAirAbove = spread.getBoolean("requires_air_above", false);
    int maxNearby = spread.getInt("max_nearby", 64);
    int lightMin = spread.getInt("conditions.light_min", 0);
    int lightMax = spread.getInt("conditions.light_max", 15);
    String defaultResult = spread.getString("result", "self");
    boolean protectionEnabled = spread.getBoolean("protection.enabled", false);
    boolean respectClaims = spread.getBoolean("protection.respect_claims", true);

    Spread.Mode mode = Spread.Mode.SINGLE;
    String rawMode = spread.getString("mode", "SINGLE");
    try {
      mode = Spread.Mode.valueOf(rawMode.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      NexoAddon.getInstance().getLogger().warning("Unknown spread `mode` `" + rawMode
          + "` on `" + itemId + "`. Falling back to SINGLE.");
    }

    List<Spread.Rule> rules = new ArrayList<>();
    for (Map<?, ?> raw : spread.getMapList("rules")) {
      Object result = raw.get("result");
      Spread.Rule rule = buildSpreadRule(
          flattenStrings(raw.get("replace")),
          result instanceof String string && !string.isBlank() ? string : defaultResult,
          itemId
      );
      if (rule != null) rules.add(rule);
    }

    Spread.Rule legacy = buildSpreadRule(spread.getStringList("replace"), defaultResult, itemId);
    if (legacy != null) rules.add(legacy);

    if (rules.isEmpty()) {
      NexoAddon.getInstance().getLogger().warning("Spread mechanic on `" + itemId
          + "` has no valid `rules` or `replace` entries. Skipping.");
      return;
    }

    List<String> biomes = new ArrayList<>();
    for (String raw : spread.getStringList("conditions.biome")) {
      biomes.add(raw.toLowerCase(Locale.ROOT));
    }

    mechanic.setSpread(new Spread(interval, chance, radius, requiresAirAbove, maxNearby,
        lightMin, lightMax, biomes, mode, protectionEnabled, respectClaims, List.copyOf(rules)));
    NexoAddon.instance.setIsSpread(true);
  }

  private static List<String> flattenStrings(Object raw) {
    if (raw == null) return List.of();
    if (raw instanceof List<?> list) {
      List<String> values = new ArrayList<>(list.size());
      for (Object entry : list) {
        if (entry != null) values.add(String.valueOf(entry));
      }
      return values;
    }
    return List.of(String.valueOf(raw));
  }

  private static Spread.Rule buildSpreadRule(List<String> entries, String result, String itemId) {
    boolean wildcard = false;
    Set<Material> materials = EnumSet.noneOf(Material.class);
    List<Tag<Material>> tags = new ArrayList<>();

    for (String raw : entries) {
      if (raw == null || raw.isBlank()) continue;

      String value = raw.trim();
      if (value.equals("*") || value.equalsIgnoreCase("all") || value.equalsIgnoreCase("any")) {
        wildcard = true;
        continue;
      }

      boolean forcedTag = value.startsWith("#");
      if (forcedTag) value = value.substring(1);

      if (!forcedTag) {
        Material material = Material.matchMaterial(value);
        if (material != null) {
          materials.add(material);
          continue;
        }
      }

      Tag<Material> tag = matchBlockTag(value);
      if (tag != null) {
        tags.add(tag);
        continue;
      }

      NexoAddon.getInstance().getLogger().warning("Unknown material or block tag `" + raw
          + "` in spread rules on `" + itemId + "`. Skipping it.");
    }

    if (!wildcard && materials.isEmpty() && tags.isEmpty()) return null;
    return new Spread.Rule(wildcard, Collections.unmodifiableSet(materials), List.copyOf(tags), result);
  }

  private static Tag<Material> matchBlockTag(String value) {
    NamespacedKey key;
    try {
      key = value.indexOf(':') >= 0
          ? NamespacedKey.fromString(value.toLowerCase(Locale.ROOT))
          : NamespacedKey.minecraft(value.toLowerCase(Locale.ROOT));
    } catch (Throwable ignored) {
      return null;
    }
    if (key == null) return null;

    try {
      Tag<Material> blocks = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
      if (blocks != null) return blocks;
      return Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material.class);
    } catch (Throwable ignored) {
      return null;
    }
  }

  private static void parseItemList(List<String> rawItems, List<Material> materials, List<String> nexoIds) {
    for (String rawItem : rawItems) {
      Material material = Material.matchMaterial(rawItem);
      if (material != null) {
        materials.add(material);
        continue;
      }
      if (NexoItems.itemFromId(rawItem) != null) {
        nexoIds.add(rawItem);
      }
    }
  }

}
