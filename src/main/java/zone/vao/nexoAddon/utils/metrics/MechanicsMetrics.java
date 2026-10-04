package zone.vao.nexoAddon.utils.metrics;

import zone.vao.nexoAddon.items.Mechanics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MechanicsMetrics {

  private static final Map<String, Predicate<Mechanics>> mechanics = Map.ofEntries(
      Map.entry("Repair", mechanic -> mechanic.getRepair() != null),
      Map.entry("BigMining", mechanic -> mechanic.getBigMining() != null),
      Map.entry("VeinMiner", mechanic -> mechanic.getVeinMiner() != null),
      Map.entry("BedrockBreak", mechanic -> mechanic.getBedrockBreak() != null),
      Map.entry("Aura", mechanic -> mechanic.getAura() != null && !mechanic.getAura().isEmpty()),
      Map.entry("SpawnerBreak", mechanic -> mechanic.getSpawnerBreak() != null),
      Map.entry("MiningTools", mechanic -> mechanic.getMiningTools() != null),
      Map.entry("DropExperience", mechanic -> mechanic.getDropExperience() != null),
      Map.entry("Infested", mechanic -> mechanic.getInfested() != null),
      Map.entry("KillMessage", mechanic -> mechanic.getKillMessage() != null),
      Map.entry("Stackable", mechanic -> mechanic.getStackable() != null),
      Map.entry("Decay", mechanic -> mechanic.getDecay() != null),
      Map.entry("ShiftBlock", mechanic -> mechanic.getShiftBlock() != null),
      Map.entry("BottledExp", mechanic -> mechanic.getBottledExp() != null),
      Map.entry("Unstackable", mechanic -> mechanic.getUnstackable() != null),
      Map.entry("BlockAura", mechanic -> mechanic.getBlockAura() != null),
      Map.entry("Signal", mechanic -> mechanic.getSignal() != null),
      Map.entry("Remember", mechanic -> mechanic.getRemember() != null && mechanic.getRemember().isForRemember()),
      Map.entry("Enchantify", mechanic -> mechanic.getEnchantify() != null),
      Map.entry("AutoCatch", mechanic -> mechanic.getAutoCatch() != null),
      Map.entry("UniqueId", mechanic -> mechanic.getUniqueId() != null && mechanic.getUniqueId().enabled()),
      Map.entry("InventoryType", mechanic -> mechanic.getInventoryType() != null),
      Map.entry("CustomCrafting", mechanic -> mechanic.getCustomCrafting() != null),
      Map.entry("Lifesteal", mechanic -> mechanic.getLifesteal() != null),
      Map.entry("Spread", mechanic -> mechanic.getSpread() != null),
      Map.entry("Thor", mechanic -> mechanic.getThor() != null),
      Map.entry("Liquid", mechanic -> mechanic.getLiquid() != null),
      Map.entry("Fuel", mechanic -> mechanic.getFuel() != null)
  );

  private static volatile Map<String, Integer> items = Map.of();

  public static void update(Collection<Mechanics> loadedMechanics) {
    Map<String, Integer> counts = new HashMap<>();
    mechanics.forEach((name, predicate) -> counts.put(name, 0));

    for (Mechanics mechanic : loadedMechanics) {
      mechanics.forEach((name, predicate) -> {
        if (predicate.test(mechanic)) counts.merge(name, 1, Integer::sum);
      });
    }
    items = Map.copyOf(counts);
  }

  public static Map<String, int[]> getUsage() {
    Map<String, int[]> usage = new HashMap<>();
    items.forEach((name, count) -> usage.put(name, count > 0 ? new int[]{1, 0} : new int[]{0, 1}));
    return usage;
  }

  public static Map<String, Integer> getItems() {
    return items;
  }
}
