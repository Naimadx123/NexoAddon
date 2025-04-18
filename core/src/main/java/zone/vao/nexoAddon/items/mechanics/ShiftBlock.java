package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockPlaceEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static zone.vao.nexoAddon.utils.BlockUtil.startShiftBlock;

public record ShiftBlock(String replaceTo, int time, List<Material> materials, List<String> nexoIds, Boolean onInteract, Boolean onBreak, Boolean onPlace) {

  public static class ShiftBlockListener implements Listener {
    public static List<UUID> toCancelation = Collections.synchronizedList(new ArrayList<>());

    @EventHandler
    public static void onShiftBlockPlaceFurniture(NexoFurniturePlaceEvent event) {
      if (toCancelation.contains(event.getPlayer().getUniqueId())) {
        event.setCancelled(true);
        toCancelation.remove(event.getPlayer().getUniqueId());
        return;
      }
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onPlace())
        return;
      FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(mechanics.getShiftBlock().replaceTo());
      if (furnitureMechanic == null)
        return;
      ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          mechanics.getShiftBlock().materials().contains(itemStack.getType()) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack)))
        return;
      event.setCancelled(true);
      startShiftBlock(event.getBaseEntity(), furnitureMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void onShiftBlockInteractFurniture(NexoFurnitureInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND)
        return;
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onInteract())
        return;
      FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(mechanics.getShiftBlock().replaceTo());
      if (furnitureMechanic == null)
        return;
      ItemStack itemStack = event.getItemInHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          (itemStack == null || !mechanics.getShiftBlock().materials().contains(itemStack.getType())) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          (itemStack == null || !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack))))
        return;
      event.setCancelled(true);
      if (!toCancelation.contains(event.getPlayer().getUniqueId()))
        toCancelation.add(event.getPlayer().getUniqueId());

      event.getPlayer().swingMainHand();
      startShiftBlock(event.getBaseEntity(), furnitureMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }

    @EventHandler
    public static void onShiftBlockBreakFurniture(NexoFurnitureBreakEvent event) {
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onBreak())
        return;
      FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(mechanics.getShiftBlock().replaceTo());
      if (furnitureMechanic == null)
        return;
      ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          mechanics.getShiftBlock().materials().contains(itemStack.getType()) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack)))
        return;
      event.setCancelled(true);
      startShiftBlock(event.getBaseEntity(), furnitureMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }

    @EventHandler
    public static void onShiftBlockPlace(NexoNoteBlockPlaceEvent event) {
      handlePlaceCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockPlace(NexoStringBlockPlaceEvent event) {
      handlePlaceCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockPlace(NexoChorusBlockPlaceEvent event) {
      handlePlaceCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockBreak(NexoNoteBlockBreakEvent event) {
      handleBreakCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockBreak(NexoStringBlockBreakEvent event) {
      handleBreakCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockBreak(NexoChorusBlockBreakEvent event) {
      handleBreakCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockInteract(NexoNoteBlockInteractEvent event) {
      handleInteractCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockInteract(NexoStringBlockInteractEvent event) {
      handleInteractCustomBlock(event);
    }

    @EventHandler
    public static void onShiftBlockInteract(NexoChorusBlockInteractEvent event) {
      handleInteractCustomBlock(event);
    }

    private static void handleInteractCustomBlock(NexoBlockInteractEvent event) {
      if (event.getHand() != EquipmentSlot.HAND)
        return;
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onInteract())
        return;
      CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(mechanics.getShiftBlock().replaceTo());
      if (customBlockMechanic == null)
        return;
      ItemStack itemStack = event.getItemInHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          (itemStack == null || !mechanics.getShiftBlock().materials().contains(itemStack.getType())) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          (itemStack == null || !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack))))
        return;

      event.getPlayer().swingMainHand();
      startShiftBlock(event.getBlock().getLocation(), customBlockMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }

    private static void handleBreakCustomBlock(NexoBlockBreakEvent event) {
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onBreak())
        return;
      CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(mechanics.getShiftBlock().replaceTo());
      if (customBlockMechanic == null)
        return;
      ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          mechanics.getShiftBlock().materials().contains(itemStack.getType()) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack)))
        return;
      startShiftBlock(event.getBlock().getLocation(), customBlockMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }

    private static void handlePlaceCustomBlock(NexoBlockPlaceEvent event){
      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
      if (mechanics == null || mechanics.getShiftBlock() == null || !mechanics.getShiftBlock().onPlace())
        return;
      CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(mechanics.getShiftBlock().replaceTo());
      if (customBlockMechanic == null)
        return;
      ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
      if (!mechanics.getShiftBlock().materials().isEmpty() &&
          mechanics.getShiftBlock().materials().contains(itemStack.getType()) &&
          !mechanics.getShiftBlock().nexoIds().isEmpty() &&
          !mechanics.getShiftBlock().nexoIds().contains(NexoItems.idFromItem(itemStack)))
        return;
      startShiftBlock(event.getBlock().getLocation(), customBlockMechanic, event.getMechanic(), mechanics.getShiftBlock().time());
    }
  }
}
