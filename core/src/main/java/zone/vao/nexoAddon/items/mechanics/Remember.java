package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

  public static boolean isRemember(String toolId) {
    return toolId != null && NexoAddon.getInstance().getMechanics().containsKey(toolId) && NexoAddon.getInstance().getMechanics().get(toolId).getRemember() != null;
  }

  public static class RememberListener implements Listener {
    @EventHandler
    public static void onFurniturePlace(NexoFurniturePlaceEvent event) {
      if (!isRemember(event.getMechanic().getItemID())) return;

      ItemStack item = event.getItemInHand();
      Component itemName = item.getItemMeta().displayName();
      List<Component> itemLore = item.getItemMeta().lore();
      PersistentDataContainer pdc = event.getBaseEntity().getPersistentDataContainer();

      String name = "";
      List<String> lore = new ArrayList<>();
      if(itemName != null)
        name = MiniMessage.miniMessage().serialize(itemName);
      if(itemLore != null) {
        for (Component component : itemLore) {
          lore.add(MiniMessage.miniMessage().serialize(component));
        }
      }

      if(!name.isEmpty())
        pdc.set(new NamespacedKey(NexoAddon.getInstance(), "name"), PersistentDataType.STRING, name);
      if(!lore.isEmpty())
        pdc.set(new NamespacedKey(NexoAddon.getInstance(), "lore"), PersistentDataType.STRING, String.join(splitter, lore));
    }

    @EventHandler
    public static void onFurnitureBreak(NexoFurnitureBreakEvent event) {
      if (!isRemember(event.getMechanic().getItemID()))
        return;

      PersistentDataContainer pdc = event.getBaseEntity().getPersistentDataContainer();
      String itemName = pdc.get(new NamespacedKey(NexoAddon.getInstance(), "name"), PersistentDataType.STRING);
      String itemLore = pdc.get(new NamespacedKey(NexoAddon.getInstance(), "lore"), PersistentDataType.STRING);

      event.setCancelled(true);

      ItemStack item = new ItemStack(NexoItems.itemFromId(event.getMechanic().getItemID()).build());

      ItemMeta im = item.getItemMeta();
      if(itemName != null && !itemName.isEmpty())
        im.displayName(MiniMessage.miniMessage().deserialize(itemName));

      if (itemLore != null && !itemLore.isEmpty()) {
        String[] lores = itemLore.split(splitter);

        List<Component> components = Arrays.stream(lores)
            .map(line -> MiniMessage.miniMessage().deserialize(line))
            .toList();

        im.lore(components);
      }

      item.setItemMeta(im);
      Loot loot = new Loot(item, 1);
      List<Loot> loots = new ArrayList<>();
      loots.add(loot);
      Drop drop = new Drop(loots, false, false, event.getMechanic().getItemID());
      NexoFurniture.remove(event.getBaseEntity(), event.getPlayer(), drop);

    }
  }
}
