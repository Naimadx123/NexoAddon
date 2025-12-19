package zone.vao.nexoAddon.events;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nexomc.nexo.NexoPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
import zone.vao.nexoAddon.items.recipes.RecipeInfo;
import zone.vao.nexoAddon.utils.VersionUtil;
import zone.vao.nexoAddon.utils.handlers.EquippableCompat;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class PrepareRecipesListener implements Listener {

  private final HashMap<UUID, NamespacedKey> preCraftedRecipes = new HashMap<>();

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPrepare(PrepareSmithingEvent event) {
    SmithingInventory inventory = event.getInventory();
    Player player = getPlayerSafe(event.getView());

    ItemStack template = inventory.getItem(0);
    ItemStack base = inventory.getItem(1);
    ItemStack addition = inventory.getItem(2);

    preCraftedRecipes.put(player.getUniqueId(), null);

    if (Stream.of(template, base, addition).anyMatch(Objects::isNull)) return;

    RecipeManager.getRegisteredRecipes().keySet().stream()
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

    RecipeInfo recipeInfo = RecipeManager.getRegisteredRecipes().get(key);

    boolean copyTrim = recipeInfo.isCopyTrim();
    boolean copyPdc = recipeInfo.isCopyPdc();
    boolean copyEnchants = recipeInfo.isCopyEnchants();
    boolean keepDurability = recipeInfo.isKeepDurability();
    boolean copyMeta = recipeInfo.isCopyMeta();

    if (copyMeta) {
      resultMeta = buildCopiedMeta(baseMeta, resultMeta);
    }

    if (copyEnchants) {
      copyEnchantments(baseMeta, resultMeta);
    }

    if (copyTrim) {
      copyTrim(baseMeta, resultMeta);
    }

    if (copyPdc) {
      copyPdc(baseMeta, resultMeta);
    }

    if (keepDurability) {
      copyDurability(baseMeta, resultMeta);
    }

    result.setItemMeta(resultMeta);
  }

  private ItemMeta buildCopiedMeta(ItemMeta baseMeta, ItemMeta resultMeta) {
    ItemMeta baseMetaClone = baseMeta.clone();

    copyNexoIdAndBasicFields(baseMetaClone, resultMeta);
    copyDamageMeta(baseMetaClone, resultMeta);
    copyEquippableMeta(baseMetaClone, resultMeta);
    mergeLore(baseMetaClone, resultMeta);
    mergeAttributes(baseMetaClone, resultMeta);

    return baseMetaClone;
  }

  private void copyNexoIdAndBasicFields(ItemMeta target, ItemMeta source) {
    String nexoId = source.getPersistentDataContainer()
        .get(new NamespacedKey("nexo", "id"), PersistentDataType.STRING);
    if (nexoId != null) {
      target.getPersistentDataContainer()
          .set(new NamespacedKey("nexo", "id"), PersistentDataType.STRING, nexoId);
    }

    if (source.hasCustomModelData()) {
      target.setCustomModelData(source.getCustomModelData());
    }
    if (source.hasDisplayName()) {
      target.setDisplayName(source.getDisplayName());
    }
    if (source.hasItemName()) {
      target.itemName(source.itemName());
    }
  }

  private void copyDamageMeta(ItemMeta target, ItemMeta source) {
    if (!(source instanceof Damageable damageable) ||
        !(target instanceof Damageable baseDamageable)) {
      return;
    }

    if (damageable.hasMaxDamage()) {
      baseDamageable.setMaxDamage(damageable.getMaxDamage());
    }
    if (damageable.hasDamage()) {
      baseDamageable.setDamage(damageable.getDamage());
    }
  }

  private void copyEquippableMeta(ItemMeta target, ItemMeta source) {
    if (VersionUtil.isVersionLessThan("1.21.2")) return;

    Object eq = EquippableCompat.getEquippable(source);
    if (eq != null || EquippableCompat.hasEquippable(source)) {
      EquippableCompat.setEquippable(target, eq);
    }
  }

  private void mergeLore(ItemMeta target, ItemMeta source) {
    if (!source.hasLore()) return;

    java.util.List<Component> merged = new java.util.ArrayList<>();
    java.util.List<Component> baseLore = target.lore();
    if (baseLore != null && !baseLore.isEmpty()) {
      merged.addAll(baseLore);
    }
    java.util.List<Component> resultLore = source.lore();
    if (resultLore != null && !resultLore.isEmpty()) {
      merged.addAll(resultLore);
    }
    target.lore(merged);
  }

  private void mergeAttributes(ItemMeta target, ItemMeta source) {
    if (!source.hasAttributeModifiers()) return;

    Multimap<Attribute, AttributeModifier> toAdd = source.getAttributeModifiers();
    if (toAdd == null || toAdd.isEmpty()) return;

    Multimap<Attribute, AttributeModifier> merged =
        target.getAttributeModifiers() == null
            ? HashMultimap.create()
            : HashMultimap.create(target.getAttributeModifiers());

    merged.putAll(toAdd);
    target.setAttributeModifiers(merged);
  }

  private void copyEnchantments(ItemMeta from, ItemMeta to) {
    from.getEnchants().forEach((enchant, level) ->
        to.addEnchant(enchant, level, true));
  }

  private void copyTrim(ItemMeta baseMeta, ItemMeta resultMeta) {
    if (!(baseMeta instanceof ArmorMeta baseArmorMeta)) return;
    if (!baseArmorMeta.hasTrim()) return;
    if (resultMeta instanceof ArmorMeta armorMeta) {
      armorMeta.setTrim(baseArmorMeta.getTrim());
    }
  }

  private void copyPdc(ItemMeta baseMeta, ItemMeta resultMeta) {
    baseMeta.getPersistentDataContainer()
        .copyTo(resultMeta.getPersistentDataContainer(), false);
  }

  private void copyDurability(ItemMeta baseMeta, ItemMeta resultMeta) {
    if (!(resultMeta instanceof Damageable damageable) ||
        !(baseMeta instanceof Damageable baseDamageable)) {
      return;
    }
    damageable.setDamage(baseDamageable.getDamage());
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

  public Player getPlayerSafe(InventoryView view) {
    try {
      return (Player) InventoryView.class.getMethod("getPlayer").invoke(view);
    } catch (Throwable t) {
      return (Player) view.getPlayer();
    }
  }

}
