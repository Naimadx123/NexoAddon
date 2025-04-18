package zone.vao.nexoAddon.events.nexo.furnitures.breaks;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;

import java.util.List;

public class BreakWithToolsListener {

  public static void onBreakWithTools(NexoFurnitureBreakEvent event){

    Player p = event.getPlayer();
    ItemStack item = p.getInventory().getItemInMainHand();

    if(NexoAddon.getInstance().getGlobalConfig().getBoolean("prevent_furniture_break_with_tools", true)){
      if(isTool(item.getType()))
        event.setCancelled(true);
    }

    if(NexoAddon.getInstance().getGlobalConfig().getBoolean("prevent_furniture_break_with_sword", true)){
      if(isSword(item.getType()))
        event.setCancelled(true);
    }
  }

  private static boolean isTool(Material item){
    return pickaxes.contains(item) ||
        axes.contains(item) || shovels.contains(item) || hoes.contains(item);
  }

  private static boolean isSword(Material item){
    return swords.contains(item);
  }

  private static final List<Material> swords = List.of(
      Material.DIAMOND_SWORD,
      Material.WOODEN_SWORD,
      Material.GOLDEN_SWORD,
      Material.IRON_SWORD,
      Material.STONE_SWORD,
      Material.NETHERITE_SWORD
  );

  private static final List<Material> pickaxes = List.of(
      Material.DIAMOND_PICKAXE,
      Material.WOODEN_PICKAXE,
      Material.GOLDEN_PICKAXE,
      Material.IRON_PICKAXE,
      Material.STONE_PICKAXE,
      Material.NETHERITE_PICKAXE
  );

  private static final List<Material> axes = List.of(
      Material.DIAMOND_AXE,
      Material.WOODEN_AXE,
      Material.GOLDEN_AXE,
      Material.IRON_AXE,
      Material.STONE_AXE,
      Material.NETHERITE_AXE
  );

  private static final List<Material> shovels = List.of(
      Material.DIAMOND_SHOVEL,
      Material.WOODEN_SHOVEL,
      Material.GOLDEN_SHOVEL,
      Material.IRON_SHOVEL,
      Material.STONE_SHOVEL,
      Material.NETHERITE_SHOVEL
  );

  private static final List<Material> hoes = List.of(
      Material.DIAMOND_HOE,
      Material.WOODEN_HOE,
      Material.GOLDEN_HOE,
      Material.IRON_HOE,
      Material.STONE_HOE,
      Material.NETHERITE_HOE
  );
}
