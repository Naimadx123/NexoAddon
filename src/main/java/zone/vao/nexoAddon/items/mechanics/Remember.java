package zone.vao.nexoAddon.items.mechanics;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Remember(boolean isForRemember) {

  private static final String splitter = "/@/";
  private static final MiniMessage MM = MiniMessage.miniMessage();
  private static final NamespacedKey NAME_KEY = new NamespacedKey(NexoAddon.getInstance(), "name");
  private static final NamespacedKey LORE_KEY = new NamespacedKey(NexoAddon.getInstance(), "lore");

  public static boolean isRemember(String toolId) {
    return toolId != null && NexoAddon.getInstance().getMechanics().containsKey(toolId) && NexoAddon.getInstance().getMechanics().get(toolId).getRemember() != null;
  }

  private static String serializeName(Component name) {
    return name == null ? "" : MM.serialize(name);
  }

  private static List<String> serializeLore(List<Component> lore) {
    List<String> result = new ArrayList<>();
    if (lore == null) return result;
    for (Component c : lore) {
      result.add(MM.serialize(c));
    }
    return result;
  }

  private static void writeNameLore(PersistentDataContainer pdc, String name, List<String> lore) {
    if (name != null && !name.isEmpty()) {
      pdc.set(NAME_KEY, PersistentDataType.STRING, name);
    }
    if (lore != null && !lore.isEmpty()) {
      pdc.set(LORE_KEY, PersistentDataType.STRING, String.join(splitter, lore));
    }
  }

  private static String readStoredName(PersistentDataContainer pdc) {
    return pdc.get(NAME_KEY, PersistentDataType.STRING);
  }

  private static String readStoredLore(PersistentDataContainer pdc) {
    return pdc.get(LORE_KEY, PersistentDataType.STRING);
  }

  private static void applyNameLore(ItemMeta meta, String serializedName, String serializedLore) {
    if (serializedName != null && !serializedName.isEmpty()) {
      meta.displayName(MM.deserialize(serializedName));
    }
    if (serializedLore != null && !serializedLore.isEmpty()) {
      String[] lores = serializedLore.split(splitter);
      List<Component> components = Arrays.stream(lores)
          .map(MM::deserialize)
          .toList();
      meta.lore(components);
    }
  }

  private static ItemStack baseItem(String itemId) {
    return new ItemStack(NexoItems.itemFromId(itemId).build());
  }

  private static Drop singleItemDrop(ItemStack item, String sourceId) {
    Loot loot = new Loot(item, 1);
    List<Loot> loots = new ArrayList<>();
    loots.add(loot);
    return new Drop(loots, false, false, sourceId);
  }

  public static class RememberListener implements Listener {
    @EventHandler
    public static void onFurniturePlace(NexoFurniturePlaceEvent event) {
      if (!isRemember(event.getMechanic().getItemID())) return;

      ItemStack item = event.getItemInHand();
      Component itemName = item.getItemMeta().displayName();
      List<Component> itemLore = item.getItemMeta().lore();
      PersistentDataContainer pdc = event.getBaseEntity().getPersistentDataContainer();

      String name = serializeName(itemName);
      List<String> lore = serializeLore(itemLore);
      writeNameLore(pdc, name, lore);
    }

    @EventHandler
    public static void onFurnitureBreak(NexoFurnitureBreakEvent event) {
      if (!isRemember(event.getMechanic().getItemID())) return;

      PersistentDataContainer pdc = event.getBaseEntity().getPersistentDataContainer();
      String itemName = readStoredName(pdc);
      String itemLore = readStoredLore(pdc);

      event.setCancelled(true);

      ItemStack item = baseItem(event.getMechanic().getItemID());
      ItemMeta im = item.getItemMeta();
      applyNameLore(im, itemName, itemLore);
      item.setItemMeta(im);

      Drop drop = singleItemDrop(item, event.getMechanic().getItemID());
      NexoFurniture.remove(event.getBaseEntity(), event.getPlayer(), drop);
    }

    @EventHandler
    public static void onBlockPlace(NexoBlockPlaceEvent event) {
      if (!isRemember(event.getMechanic().getItemID())) return;

      ItemStack item = event.getItemInHand();
      Component itemName = item.getItemMeta().displayName();
      List<Component> itemLore = item.getItemMeta().lore();
      PersistentDataContainer pdc = new CustomBlockData(event.getBlock(), NexoAddon.getInstance());

      String name = serializeName(itemName);
      List<String> lore = serializeLore(itemLore);
      writeNameLore(pdc, name, lore);
    }

    @EventHandler
    public static void onBlockBreak(NexoBlockBreakEvent event) {
      if (!isRemember(event.getMechanic().getItemID())) return;

      PersistentDataContainer pdc = new CustomBlockData(event.getBlock(), NexoAddon.getInstance());
      String storedName = readStoredName(pdc);
      String storedLore = readStoredLore(pdc);

      event.setCancelled(true);

      ItemStack item = baseItem(event.getMechanic().getItemID());
      ItemMeta im = item.getItemMeta();
      applyNameLore(im, storedName, storedLore);
      item.setItemMeta(im);

      Drop drop = singleItemDrop(item, event.getMechanic().getItemID());
      NexoBlocks.remove(event.getBlock().getLocation(), event.getPlayer(), drop);
    }
  }
}
