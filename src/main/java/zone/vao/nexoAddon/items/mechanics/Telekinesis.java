package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public record Telekinesis(boolean enabled, List<Material> materials, List<String> nexoIds) {

    public static boolean hasTelekinesis(String toolId) {
        if (toolId == null) {
            return false;
        }
        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(toolId);
        return mechanics != null && mechanics.getTelekinesis() != null && mechanics.getTelekinesis().enabled();
    }

    public static class TelekinesisListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public static void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();

            ItemStack tool = player.getInventory().getItemInMainHand();
            String toolId = NexoItems.idFromItem(tool);

            if (!hasTelekinesis(toolId)) {
                return;
            }

            if (!event.isDropItems()) {
                return;
            }

            Block block = event.getBlock();
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(toolId);
            Telekinesis telekinesis = mechanics.getTelekinesis();

            // Whitelist: Wenn materials oder nexoIds konfiguriert sind,
            // darf Telekinesis nur für Blöcke auf der Liste greifen.
            if (!telekinesis.materials().isEmpty() || !telekinesis.nexoIds().isEmpty()) {
                boolean allowed = telekinesis.materials().contains(block.getType());

                if (!allowed && !telekinesis.nexoIds().isEmpty()) {
                    CustomBlockMechanic blockMechanic = NexoBlocks.customBlockMechanic(block);
                    if (blockMechanic != null) {
                        allowed = telekinesis.nexoIds().contains(blockMechanic.getItemID());
                    }
                }

                if (!allowed) {
                    return;
                }
            }

            // Verhindere normale Drops
            event.setDropItems(false);

            // Sammle die Drops die der Block normalerweise geben würde
            Collection<ItemStack> drops = block.getDrops(tool, player);

            // Füge die Items direkt zum Inventar hinzu
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drops.toArray(new ItemStack[0]));

            // Wenn das Inventar voll ist, droppe die übrigen Items
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }
    }
}
