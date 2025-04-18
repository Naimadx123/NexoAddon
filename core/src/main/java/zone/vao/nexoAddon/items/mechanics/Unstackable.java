package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.custom_block.stringblock.StringBlockMechanic;
import com.nexomc.nexo.utils.blocksounds.BlockSounds;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.ArrayList;
import java.util.List;

public record Unstackable(String next, String give, List<Material> materials, List<String> nexoIds) {

  public static class UnstackableListener implements Listener {

    @EventHandler
    public static void onUntackable(final NexoStringBlockInteractEvent event) {
      if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

      Mechanics mechanic = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());

      if(event.getHand() != EquipmentSlot.HAND) return;

      if(mechanic == null
          || mechanic.getUnstackable() == null
          || !checkIfAllowed(mechanic, event.getPlayer())
          || !ProtectionLib.canInteract(event.getPlayer(), event.getBlock().getLocation())
      ) return;

      String nextStage = mechanic.getUnstackable().next();
      StringBlockMechanic newBlock = NexoBlocks.stringMechanic(nextStage);

      if(newBlock == null && !nextStage.equalsIgnoreCase("stop")) return;

      event.setCancelled(true);
      List<Loot> loots = new ArrayList<>();
      Drop drop = new Drop(loots, false, false, newBlock != null ? newBlock.getItemID() : event.getMechanic().getItemID());
      NexoBlocks.remove(event.getBlock().getLocation(), null, drop);

      BlockSounds blockSounds = newBlock.getBlockSounds();
      if(blockSounds != null && blockSounds.hasBreakSound()){
        event.getBlock().getWorld().playSound(event.getBlock().getLocation(), blockSounds.getBreakSound(), SoundCategory.BLOCKS, blockSounds.getBreakVolume(), blockSounds.getBreakPitch());
      }
      if(!nextStage.equalsIgnoreCase("stop")) {
        NexoBlocks.place(nextStage, event.getBlock().getLocation());
      }
      event.getPlayer().swingMainHand();

      Material material = Material.matchMaterial(mechanic.getUnstackable().give());
      if(material != null){

        event.getPlayer().getInventory().addItem(new ItemStack(material));
        return;
      }
      ItemBuilder itemBuilder = NexoItems.itemFromId(mechanic.getUnstackable().give());
      if(itemBuilder != null){
        event.getPlayer().getInventory().addItem(itemBuilder.build());
      }
    }

    private static boolean checkIfAllowed(Mechanics mechanic, Player player) {
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
  }
}
