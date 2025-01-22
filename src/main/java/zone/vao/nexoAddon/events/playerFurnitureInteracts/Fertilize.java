package zone.vao.nexoAddon.events.playerFurnitureInteracts;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import io.th0rgal.protectionlib.ProtectionLib;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Components;
import zone.vao.nexoAddon.classes.component.Fertilizer;
import zone.vao.nexoAddon.utils.InventoryUtil;
import zone.vao.nexoAddon.utils.ParticleUtil;

public class Fertilize {

  @Getter
  static final NamespacedKey EVOLUTION_KEY = new NamespacedKey(NexoPlugin.instance(), "evolution");

  public static void onFertilize(NexoFurnitureInteractEvent event){

    Player player = event.getPlayer();
    String furnitureId = NexoFurniture.furnitureMechanic(event.getBaseEntity()).getItemID();

    String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
    if(NexoAddon.getInstance().getComponents() == null
        || NexoAddon.getInstance().getComponents().get(itemId) == null) return;
    Fertilizer fertilizer = NexoAddon.getInstance().getComponents().get(itemId).getFertilizer();
    if(itemId == null
        || !NexoAddon.getInstance().getComponents().containsKey(itemId)
        || fertilizer == null
        || !fertilizer.getUsableOn().contains(furnitureId)
        || !event.getBaseEntity().getPersistentDataContainer().has(EVOLUTION_KEY, PersistentDataType.INTEGER)
        || (event.getBaseEntity().getPersistentDataContainer().get(EVOLUTION_KEY, PersistentDataType.INTEGER) >= NexoFurniture.furnitureMechanic(event.getBaseEntity()).getEvolution().getDelay())
        || !(ProtectionLib.canInteract(player, event.getBaseEntity().getLocation()) && ProtectionLib.canUse(player, event.getBaseEntity().getLocation()))
    ) return;
    fertilizeFurniture(event.getBaseEntity(), player, NexoAddon.getInstance().getComponents().get(itemId));
  }

  private static void fertilizeFurniture(ItemDisplay itemDisplay, Player player, Components component) {

    PersistentDataContainer container = itemDisplay.getPersistentDataContainer();

    int evolutionTime = container.get(EVOLUTION_KEY, PersistentDataType.INTEGER);

    evolutionTime += component.getFertilizer().getGrowthSpeedup();

    container.set(EVOLUTION_KEY, PersistentDataType.INTEGER, evolutionTime);

    if(NexoItems.itemFromId(component.getId()).getDurability() == null
        || NexoItems.itemFromId(component.getId()).getDurability() <= 1
        || ((Damageable) player.getInventory().getItemInMainHand().getItemMeta()).hasDamage()
            && ((Damageable) player.getInventory().getItemInMainHand().getItemMeta()).getDamage() >= NexoItems.itemFromId(component.getId()).getDurability()
    ) {
      InventoryUtil.removePartialStack(player, player.getInventory().getItemInMainHand(), 1);
    }else{
      int maxDurability = NexoItems.itemFromId(component.getId()).getDurability() != null ? NexoItems.itemFromId(component.getId()).getDurability() : NexoItems.itemFromId(component.getId()).build().getType().getMaxDurability();
      Damageable itemMeta = (Damageable) player.getInventory().getItemInMainHand().getItemMeta();
      itemMeta.setDamage(itemMeta.getDamage()+1);
      player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
      if(maxDurability <= itemMeta.getDamage()){
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
      }
    }

    player.spawnParticle(ParticleUtil.getHappyVillagerParticle(), itemDisplay.getLocation(), 10, 0.5, 0.5, 0.5);
    NexoFurniture.updateFurniture(itemDisplay);
  }
}
