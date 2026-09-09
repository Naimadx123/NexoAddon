package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.CustomCraftingUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CustomCrafting(
    Component title,
    int size,
    int resultSlot,
    String station,
    ItemStack filler
) {

  public record Ingredient(String nexoId, Material material, int amount) {

    public boolean matches(ItemStack item) {
      if (item == null || item.getType().isAir() || item.getAmount() < amount) return false;

      String itemId = NexoItems.idFromItem(item);
      if (nexoId != null) return nexoId.equals(itemId);

      return itemId == null && item.getType() == material;
    }
  }

  public record Recipe(Map<Integer, Ingredient> ingredients, ItemStack result) {
  }

  public Inventory create() {
    Holder holder = new Holder(this);
    Inventory inventory = Bukkit.createInventory(holder, size, title);
    holder.inventory = inventory;

    if (filler != null) {
      for (int slot = 0; slot < size; slot++) {
        if (slot == resultSlot || holder.inputSlots.contains(slot)) continue;
        inventory.setItem(slot, filler.clone());
      }
    }
    return inventory;
  }

  private Set<Integer> inputSlots(List<Recipe> recipes) {
    Set<Integer> slots = new LinkedHashSet<>();
    for (Recipe recipe : recipes) {
      for (int slot : recipe.ingredients().keySet()) {
        if (slot < size && slot != resultSlot) slots.add(slot);
      }
    }
    return slots;
  }

  public static class Holder implements InventoryHolder {

    private final CustomCrafting crafting;
    private final List<Recipe> recipes;
    private final Set<Integer> inputSlots;
    private Inventory inventory;

    private Holder(CustomCrafting crafting) {
      this.crafting = crafting;
      this.recipes = CustomCraftingUtil.getRecipes(crafting.station());
      this.inputSlots = crafting.inputSlots(recipes);
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }

    private Recipe match() {
      for (Recipe recipe : recipes) {
        if (matches(recipe)) return recipe;
      }
      return null;
    }

    private boolean matches(Recipe recipe) {
      if (!inputSlots.containsAll(recipe.ingredients().keySet())) return false;

      for (int slot : inputSlots) {
        ItemStack item = inventory.getItem(slot);
        Ingredient ingredient = recipe.ingredients().get(slot);

        if (ingredient == null) {
          if (item != null && !item.getType().isAir()) return false;
          continue;
        }
        if (!ingredient.matches(item)) return false;
      }
      return true;
    }
  }

  public static class CustomCraftingListener implements Listener {

    @EventHandler
    public void on(NexoBlockInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK)
        return;
      openMenu(event.getPlayer(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void on(NexoFurnitureInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND)
        return;
      openMenu(event.getPlayer(), event.getMechanic().getItemID());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
      if (!(event.getInventory().getHolder() instanceof Holder holder)) return;

      Player player = (Player) event.getWhoClicked();

      if (event.getClick() == ClickType.DOUBLE_CLICK) {
        event.setCancelled(true);
        return;
      }

      if (event.getClickedInventory() != event.getInventory()) {
        if (event.isShiftClick()) {
          event.setCancelled(true);
          moveToInput(event, holder);
        }
        updateResult(player, holder);
        return;
      }

      int slot = event.getSlot();
      if (slot == holder.crafting.resultSlot()) {
        event.setCancelled(true);
        craft(player, holder);
        return;
      }

      if (!holder.inputSlots.contains(slot)) {
        event.setCancelled(true);
        return;
      }
      updateResult(player, holder);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
      if (!(event.getInventory().getHolder() instanceof Holder holder)) return;

      for (int rawSlot : event.getRawSlots()) {
        if (rawSlot >= holder.crafting.size()) continue;
        if (holder.inputSlots.contains(rawSlot)) continue;

        event.setCancelled(true);
        return;
      }
      updateResult((Player) event.getWhoClicked(), holder);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
      if (!(event.getInventory().getHolder() instanceof Holder holder)) return;

      Inventory inventory = holder.getInventory();
      Player player = (Player) event.getPlayer();

      for (int slot : holder.inputSlots) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType().isAir()) continue;

        inventory.setItem(slot, null);
        giveOrDrop(player, item);
      }
      inventory.setItem(holder.crafting.resultSlot(), null);
    }

    private void openMenu(Player player, String nexoItemId) {
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
      if (mechanics == null || mechanics.getCustomCrafting() == null)
        return;

      player.openInventory(mechanics.getCustomCrafting().create());
    }

    private void moveToInput(InventoryClickEvent event, Holder holder) {
      ItemStack item = event.getCurrentItem();
      if (item == null || item.getType().isAir()) return;

      Inventory inventory = holder.getInventory();
      for (int slot : holder.inputSlots) {
        ItemStack current = inventory.getItem(slot);
        if (current != null && !current.getType().isAir()) continue;

        inventory.setItem(slot, item.clone());
        event.setCurrentItem(null);
        ((Player) event.getWhoClicked()).updateInventory();
        return;
      }
    }

    private void craft(Player player, Holder holder) {
      Inventory inventory = holder.getInventory();
      Recipe recipe = holder.match();
      if (recipe == null) return;

      recipe.ingredients().forEach((slot, ingredient) -> {
        ItemStack item = inventory.getItem(slot);
        if (item == null) return;

        if (item.getAmount() <= ingredient.amount()) inventory.setItem(slot, null);
        else item.setAmount(item.getAmount() - ingredient.amount());
      });

      inventory.setItem(holder.crafting.resultSlot(), null);
      giveOrDrop(player, recipe.result().clone());
      updateResult(player, holder);
    }

    private void updateResult(Player player, Holder holder) {
      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityLater(player, task -> {
        Recipe recipe = holder.match();

        holder.getInventory().setItem(holder.crafting.resultSlot(), recipe == null ? null : recipe.result().clone());
      }, 1L);
    }

    private void giveOrDrop(Player player, ItemStack item) {
      player.getInventory().addItem(item).values()
          .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
  }
}
