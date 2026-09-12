package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.List;

public record Fuel(int burnTime, List<Material> furnaces, String leftover) {

  public static final int INFINITE = -1;

  public boolean infinite() {
    return burnTime == INFINITE;
  }

  public ItemStack leftoverItem() {
    if (leftover == null) return null;

    Material material = Material.matchMaterial(leftover);
    if (material != null) return new ItemStack(material);

    ItemBuilder itemBuilder = NexoItems.itemFromId(leftover);
    return itemBuilder == null ? null : itemBuilder.build();
  }

  public static class FuelListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBurn(FurnaceBurnEvent event) {
      if (NexoAddon.getInstance().getMechanics().isEmpty()) return;

      String itemId = NexoItems.idFromItem(event.getFuel());
      if (itemId == null || itemId.isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
      if (mechanics == null) return;

      Fuel fuel = mechanics.getFuel();
      if (fuel == null) return;

      if (!fuel.furnaces().isEmpty() && !fuel.furnaces().contains(event.getBlock().getType())) {
        event.setCancelled(true);
        return;
      }

      if (fuel.infinite()) {
        event.setBurnTime(Short.MAX_VALUE);
        event.setConsumeFuel(false);
        return;
      }

      event.setBurnTime(fuel.burnTime());

      ItemStack leftover = fuel.leftoverItem();
      if (leftover == null) return;
      if (!(event.getBlock().getState() instanceof Furnace furnace)) return;

      event.setConsumeFuel(false);
      ItemStack slot = event.getFuel();
      if (slot.getAmount() > 1) {
        slot.setAmount(slot.getAmount() - 1);
        furnace.getWorld().dropItemNaturally(furnace.getLocation().add(0.5, 1, 0.5), leftover);
      } else {
        furnace.getInventory().setFuel(leftover);
      }
    }
  }
}
