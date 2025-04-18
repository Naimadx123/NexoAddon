package zone.vao.nexoAddon.items.components;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;
import zone.vao.nexoAddon.utils.EventUtil;
import zone.vao.nexoAddon.utils.InventoryUtil;
import zone.vao.nexoAddon.utils.ParticleUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record Fertilizer(int growthSpeedup, List<String> usableOn, int cooldown) {
  public static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
  public static final NamespacedKey EVOLUTION_KEY = new NamespacedKey(NexoPlugin.instance(), "evolution");

  public static class FertilizerListener implements Listener {

    @EventHandler
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
          || !fertilizer.usableOn.contains(furnitureId)
          || !event.getBaseEntity().getPersistentDataContainer().has(EVOLUTION_KEY, PersistentDataType.INTEGER)
          || (event.getBaseEntity().getPersistentDataContainer().get(EVOLUTION_KEY, PersistentDataType.INTEGER) >= NexoFurniture.furnitureMechanic(event.getBaseEntity()).getEvolution().getDelay())
          || !(ProtectionLib.canInteract(player, event.getBaseEntity().getLocation()) && ProtectionLib.canUse(player, event.getBaseEntity().getLocation()))
      ) return;

      Components component = NexoAddon.getInstance().getComponents().get(itemId);
      int cooldown = component.getFertilizer().cooldown;
      if (cooldown > 0) {
        long now = System.currentTimeMillis();
        if (Fertilizer.cooldowns.containsKey(player.getUniqueId()) && Fertilizer.cooldowns.get(player.getUniqueId()) > now) {
          return;
        }
      }

      if (cooldown > 0) {
        Fertilizer.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown * 1000L);
      }

      fertilizeFurniture(event.getBaseEntity(), player, component);
    }

    private static void fertilizeFurniture(ItemDisplay itemDisplay, Player player, Components component) {

      PersistentDataContainer container = itemDisplay.getPersistentDataContainer();

      int evolutionTime = container.get(EVOLUTION_KEY, PersistentDataType.INTEGER);

      evolutionTime += component.getFertilizer().growthSpeedup;

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

    @EventHandler
    public static void fertilizeVanillaCrops(PlayerInteractEvent event) {
      if (!isValidEvent(event)) return;

      Player player = event.getPlayer();
      Block clickedBlock = event.getClickedBlock();
      String itemId = NexoItems.idFromItem(event.getItem());
      Components component = NexoAddon.getInstance().getComponents().get(itemId);

      if (!canApplyFertilizer(player, clickedBlock, component)) return;

      int cooldown = component.getFertilizer().cooldown;
      if (cooldown > 0) {
        long now = System.currentTimeMillis();
        if (Fertilizer.cooldowns.containsKey(player.getUniqueId()) && Fertilizer.cooldowns.get(player.getUniqueId()) > now) {
          return;
        }
      }

      int growthSpeedup = Math.max(0, component.getFertilizer().growthSpeedup);
      boolean appliedSuccessfully = applyFertilizer(clickedBlock, growthSpeedup, event.getBlockFace());

      if (!appliedSuccessfully) return;

      if (cooldown > 0) {
        Fertilizer.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown * 1000L);
      }

      event.setCancelled(true);
      if(EventUtil.callEvent(event))
        handleItemDurability(player, component);
    }

    private static boolean isValidEvent(PlayerInteractEvent event) {
      return event.getHand() == EquipmentSlot.HAND
          && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
          && event.getClickedBlock() != null
          && event.getItem() != null
          && NexoItems.idFromItem(event.getItem()) != null;
    }

    private static boolean canApplyFertilizer(Player player, Block block, Components component) {
      if (component == null || component.getFertilizer() == null) return false;

      boolean canInteract = ProtectionLib.canInteract(player, block.getLocation()) &&
          ProtectionLib.canUse(player, block.getLocation());
      boolean isUsableOnBlock = component.getFertilizer()
          .usableOn
          .stream()
          .anyMatch(blockType -> blockType.equals("_MINECRAFT") ||
              blockType.equals(block.getType().toString().toUpperCase()));

      return canInteract && isUsableOnBlock;
    }

    private static boolean applyFertilizer(Block block, int growthSpeedup, org.bukkit.block.BlockFace blockFace) {
      boolean applied = false;
      for (int i = 0; i < growthSpeedup; i++) {
        if (block.applyBoneMeal(blockFace)) {
          applied = true;
        }
      }
      return applied;
    }

    private static void handleItemDurability(Player player, Components component) {
      int maxDurability = NexoItems.itemFromId(component.getId()).getDurability() != null
          ? NexoItems.itemFromId(component.getId()).getDurability()
          : 0;

      Damageable itemMeta = (Damageable) player.getInventory().getItemInMainHand().getItemMeta();
      if (maxDurability <= 1 || (itemMeta.hasDamage() && itemMeta.getDamage() >= maxDurability)) {
        InventoryUtil.removePartialStack(player, player.getInventory().getItemInMainHand(), 1);
      } else {
        itemMeta.setDamage(itemMeta.getDamage() + 1);
        player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
      }
    }
  }
}
