package zone.vao.nexoAddon.items.mechanics;


import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Map;

public record DropExperience(double experience, String minimalTool, String bestTool) {

  private static final Map<String, Integer> TIER_LEVELS = Map.of(
      "WOODEN", 1,
      "STONE", 2,
      "IRON", 3,
      "GOLDEN", 4,
      "DIAMOND", 5,
      "NETHERITE", 6
  );

  private static boolean meetsToolRequirements(ItemStack tool, String minimalTool, String bestTool) {
    if (minimalTool == null && bestTool == null) return true;
    if (tool.isEmpty()) return false;

    String matName = tool.getType().name();
    String[] parts = matName.split("_");

    if (parts.length != 2) return false;
    String tierStr = parts[0];
    String typeStr = parts[1];

    if (bestTool != null && !typeStr.equalsIgnoreCase(bestTool)) return false;
    if (minimalTool != null) {
      int toolTier = TIER_LEVELS.getOrDefault(tierStr.toUpperCase(), 0);
      int minTier = TIER_LEVELS.getOrDefault(minimalTool.toUpperCase(), 0);
      return toolTier >= minTier;
    }
    return true;
  }

  public static class DropExperienceListener implements Listener {

    @EventHandler
    public static void onBreak(NexoNoteBlockBreakEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getDropExperience() == null) return;

      DropExperience dropExp = mechanics.getDropExperience();
      double experience = dropExp.experience();
      String minimalTool = dropExp.minimalTool();
      String bestTool = dropExp.bestTool();

      ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
      boolean hasRequirements = meetsToolRequirements(tool, minimalTool, bestTool);

      boolean isSilktouch = tool.isEmpty() && tool.containsEnchantment(Enchantment.SILK_TOUCH);

      if (!event.isCancelled() && !(event.getPlayer().getGameMode() == GameMode.CREATIVE) && !isSilktouch && hasRequirements) {
        Location location = event.getBlock().getLocation();
        if (location.getWorld() != null) {
          ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
          orb.setExperience((int) Math.floor(experience));
        }
      }
    }
  }
}
