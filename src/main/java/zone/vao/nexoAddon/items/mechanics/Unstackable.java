package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.nexomc.nexo.utils.blocksounds.BlockSounds;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.*;

import static zone.vao.nexoAddon.utils.BlockUtil.startShiftBlock;

public record Unstackable(String next, String give, List<Material> materials, List<String> nexoIds, String take) {

  public Unstackable(String next, String give, List<Material> materials, List<String> nexoIds) {
    this(next, give, materials, nexoIds, null);
  }

  public boolean takeItem(Player player) {
    if(take == null || take.isBlank()) return true;

    ItemStack item = player.getInventory().getItemInMainHand();
    if(item == null || item.getType().isAir() || item.getAmount() < 1) return false;

    Material material = Material.matchMaterial(take);
    if(material != null) {
      if(item.getType() != material) return false;
    } else if(!take.equals(NexoItems.idFromItem(item))) return false;

    int amount = item.getAmount() - 1;
    if(amount == 0) player.getInventory().setItemInMainHand(null);
    else {
      item.setAmount(amount);
      player.getInventory().setItemInMainHand(item);
    }
    return true;
  }

  public static class UnstackableListener implements Listener {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    @EventHandler(ignoreCancelled = true)
    public static void onUntackable(final NexoStringBlockInteractEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());

      if(event.getHand() != EquipmentSlot.HAND) return;

      if(mechanic == null
          || mechanic.getUnstackable() == null
          || !checkIfAllowed(mechanic, event.getPlayer())
          || !NexoAddon.getInstance().getBoundaryGuard().canInteract(event.getPlayer(), event.getBlock().getLocation())
      ) return;

      String nextStage = mechanic.getUnstackable().next();
      StringBlockMechanic newBlock = NexoBlocks.stringMechanic(nextStage);

      if(newBlock == null && !nextStage.equalsIgnoreCase("stop")) return;
      if(!mechanic.getUnstackable().takeItem(event.getPlayer())) return;

      event.setCancelled(true);
      List<Loot> loots = new ArrayList<>();
      Drop drop = new Drop(loots, false, false, newBlock != null ? newBlock.getItemID() : event.getMechanic().getItemID());
      NexoBlocks.remove(event.getBlock().getLocation(), null, drop);

      BlockSounds blockSounds = event.getMechanic().getBlockSounds();
      if (blockSounds != null && blockSounds.hasBreakSound()) {
        event.getBlock().getWorld().playSound(event.getBlock().getLocation(), blockSounds.getBreakSound(), SoundCategory.BLOCKS, blockSounds.getBreakVolume(), blockSounds.getBreakPitch());
      }

      if(!nextStage.equalsIgnoreCase("stop")) {
        NexoBlocks.place(nextStage, event.getBlock().getLocation());
      }
      event.getPlayer().swingMainHand();

      giveItem(mechanic.getUnstackable(), event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public static void onUntackableFurniture(final NexoFurnitureInteractEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());

      if(event.getHand() != EquipmentSlot.HAND) return;

      if(mechanic == null
          || mechanic.getUnstackable() == null
          || !checkIfAllowed(mechanic, event.getPlayer())
          || !NexoAddon.getInstance().getBoundaryGuard().canInteract(event.getPlayer(), event.getBaseEntity())
      ) return;

      String nextStage = mechanic.getUnstackable().next();
      FurnitureMechanic newBlock = NexoFurniture.furnitureMechanic(nextStage);

      if(newBlock == null && !nextStage.equalsIgnoreCase("stop")) return;

      UUID uuid = event.getBaseEntity().getUniqueId();

      event.setCancelled(true);

      if (hasCooldown(uuid)) return;
      if(!mechanic.getUnstackable().takeItem(event.getPlayer())) return;
      setCooldown(uuid, 3);

      if(!nextStage.equalsIgnoreCase("stop")) {
        startShiftBlock(event.getBaseEntity(), NexoFurniture.furnitureMechanic(nextStage), event.getMechanic(), 0);
      }
      event.getPlayer().swingMainHand();

      giveItem(mechanic.getUnstackable(), event.getPlayer());
    }

    private static void giveItem(Unstackable unstackable, Player player) {
      if(unstackable.give() == null || unstackable.give().isBlank()) return;

      Material material = Material.matchMaterial(unstackable.give());
      if(material != null){
        player.getInventory().addItem(new ItemStack(material));
        return;
      }
      ItemBuilder itemBuilder = NexoItems.itemFromId(unstackable.give());
      if(itemBuilder != null){
        player.getInventory().addItem(itemBuilder.build());
      }
    }

    private static boolean checkIfAllowed(Mechanics mechanic, Player player) {
      String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
      Mechanics itemInHand = NexoAddon.getInstance().getMechanics().get(itemId);
      if(itemId != null && itemInHand != null && itemInHand.getStackable() != null && mechanic.getStackable() != null &&
          itemInHand.getStackable().group().equalsIgnoreCase(mechanic.getStackable().group())) return false;

      boolean bothListsEmpty = mechanic.getUnstackable().materials().isEmpty()
              && mechanic.getUnstackable().nexoIds().isEmpty();

      if (bothListsEmpty) {
        return true;
      }

      boolean haveMaterial = !mechanic.getUnstackable().materials().isEmpty()
              && mechanic.getUnstackable().materials().contains(player.getInventory().getItemInMainHand().getType());
      if (haveMaterial) return true;

      return !mechanic.getUnstackable().nexoIds().isEmpty()
              && mechanic.getUnstackable().nexoIds().contains(NexoItems.idFromItem(player.getInventory().getItemInMainHand()));
    }

    private static boolean hasCooldown(UUID uuid) {
      long currentTick = NexoAddon.getInstance().getServer().getCurrentTick();
      return cooldowns.getOrDefault(uuid, 0L) > currentTick;
    }

    private static void setCooldown(UUID uuid, int ticks) {
      long currentTick = NexoAddon.getInstance().getServer().getCurrentTick();
      cooldowns.put(uuid, currentTick + ticks);
    }
  }
}
