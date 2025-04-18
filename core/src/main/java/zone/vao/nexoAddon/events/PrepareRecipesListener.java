package zone.vao.nexoAddon.events;

import com.nexomc.nexo.NexoPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;
import zone.vao.nexoAddon.utils.VersionUtil;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class PrepareRecipesListener implements Listener {

  private final HashMap<UUID, NamespacedKey> preCraftedRecipes = new HashMap<>();

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPrepare(PrepareSmithingEvent event) {
    SmithingInventory inventory = event.getInventory();
    Player player = (Player) event.getView().getPlayer();

    ItemStack template = inventory.getItem(0);
    ItemStack base = inventory.getItem(1);
    ItemStack addition = inventory.getItem(2);

    preCraftedRecipes.put(player.getUniqueId(), null);

    if (Stream.of(template, base, addition).anyMatch(Objects::isNull)) return;

    RecipeManager.getRegisteredRecipes().stream()
        .sorted()
        .map(key -> (SmithingRecipe) NexoAddon.getInstance().getServer().getRecipe(key))
        .filter(Objects::nonNull)
        .filter(recipe -> matchesRecipe(((SmithingTransformRecipe) recipe), template, base, addition))
        .findFirst()
        .ifPresent(recipe -> {
          preCraftedRecipes.put(player.getUniqueId(), recipe.getKey());
          ItemStack result = recipe.getResult().clone();
          applyMetaTransformations(base, result, recipe.getKey());
          event.setResult(result);
        });
  }

  private void applyMetaTransformations(ItemStack baseItem, ItemStack result, NamespacedKey key) {
    ItemMeta baseMeta = baseItem.getItemMeta();
    ItemMeta resultMeta = result.getItemMeta();

    if (baseMeta == null || resultMeta == null) return;

    resultMeta.displayName(baseMeta.hasDisplayName() ? baseMeta.displayName() : resultMeta.displayName());
    if (!resultMeta.hasLore() && baseMeta.hasLore()) {
      resultMeta.lore(baseMeta.lore());
    }

    boolean copyTrim = RecipeManager.getRecipeConfig().getBoolean(key.getKey() + ".copy_trim", false);
    boolean copyPdc = RecipeManager.getRecipeConfig().getBoolean(key.getKey() + ".copy_pdc", false);
    boolean copyEnchants = RecipeManager.getRecipeConfig().getBoolean(key.getKey() + ".copy_enchantments", true);
    boolean keepDurability = RecipeManager.getRecipeConfig().getBoolean(key.getKey() + ".keep_durability", true);

    if (copyEnchants) {
      baseMeta.getEnchants().forEach((enchant, level) -> resultMeta.addEnchant(enchant, level, true));
    }

    if (copyTrim && baseMeta instanceof ArmorMeta baseArmorMeta) {
      if (baseArmorMeta.hasTrim()) {
        ((ArmorMeta) resultMeta).setTrim(baseArmorMeta.getTrim());
      }
    }

    if(copyPdc){
      baseMeta.getPersistentDataContainer().copyTo(resultMeta.getPersistentDataContainer(), false);
    }

    if(keepDurability && resultMeta instanceof Damageable damageable) {
      if(baseMeta instanceof Damageable baseDamageable) {
        damageable.setDamage(baseDamageable.getDamage());
      }
    }

    result.setItemMeta(resultMeta);
  }

  private boolean matchesRecipe(SmithingTransformRecipe recipe, ItemStack template, ItemStack base, ItemStack addition) {
    if (!matchesCustomItem(recipe.getTemplate(), template)) return false;

    if (!matchesCustomItem(recipe.getBase(), base)) return false;

    if (!matchesCustomItem(recipe.getAddition(), addition)) return false;

    return true;
  }

  private boolean matchesCustomItem(RecipeChoice choice, ItemStack item) {
    if (choice == null || item == null) return false;

    if (choice.test(item)) return true;

    if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
      return exactChoice.getChoices().stream().anyMatch(customItem -> {
        if (!customItem.getType().equals(item.getType())) return false;

        if (customItem.hasItemMeta() && customItem.getItemMeta().hasDisplayName()) {
          if (!Objects.equals(customItem.getItemMeta().displayName(), item.getItemMeta().displayName())) {
            return false;
          }
        }

        return hasMatchingNBT(customItem, item);
      });
    }

    return false;
  }

  private boolean hasMatchingNBT(ItemStack expected, ItemStack actual) {
    ItemMeta expectedMeta = expected.getItemMeta();
    ItemMeta actualMeta = actual.getItemMeta();

    if (expectedMeta == null || actualMeta == null) return false;

    var expectedData = expectedMeta.getPersistentDataContainer();
    var actualData = actualMeta.getPersistentDataContainer();

    String expectedNexoItem = expectedData.get(new NamespacedKey(NexoPlugin.instance(), "id"), PersistentDataType.STRING);
    String actualNexoItem = actualData.get(new NamespacedKey(NexoPlugin.instance(), "id"), PersistentDataType.STRING);

    return Objects.equals(expectedNexoItem, actualNexoItem);
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCollectResult(InventoryClickEvent event) {
    if (!(event.getClickedInventory() instanceof SmithingInventory smithingInventory)) return;

    Player player = (Player) event.getWhoClicked();
    InventoryAction action = event.getAction();
    if (event.getSlot() != (VersionUtil.isVersionLessThan("1.21") ? 2 : 3) || action.toString().contains("PLACE")) return;

    NamespacedKey recipeKey = preCraftedRecipes.get(player.getUniqueId());
    if (recipeKey == null) return;

    preCraftedRecipes.remove(player.getUniqueId());

    ItemStack result = smithingInventory.getResult();
    if (result == null) return;

    event.setCancelled(true);
    player.playSound(player, Sound.BLOCK_SMITHING_TABLE_USE, 1.0F, 1.0F);

    processIngredients(smithingInventory);

    smithingInventory.setResult(null);
    player.getInventory().addItem(result);
  }

  private void processIngredients(SmithingInventory inventory) {
    consumeItem(inventory, 0);
    consumeItem(inventory, 1);
    consumeItem(inventory, 2);
  }

  private void consumeItem(SmithingInventory inventory, int slot) {
    ItemStack item = inventory.getItem(slot);
    if (item == null || item.getAmount() <= 1) {
      inventory.setItem(slot, null);
    } else {
      item.setAmount(item.getAmount() - 1);
    }
  }
}
