package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.List;

public record SandSmelt(List<Material> sandTypes, boolean enabled, double probability) {

    public static class SandSmeltListener implements Listener {

        @EventHandler
        public static void onBlockBreak(BlockBreakEvent e) {
            Block block = e.getBlock();
            Player player = e.getPlayer();

            if (e.isCancelled() || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            ItemStack tool = player.getInventory().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(tool);

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getSandSmelt() == null) {
                return;
            }

            SandSmelt sandSmelt = mechanics.getSandSmelt();
            if (!sandSmelt.enabled()) {
                return;
            }

            // Check if the broken block is sand (vanilla or Nexo)
            boolean isSand = isSandBlock(block, sandSmelt);
            if (!isSand) {
                return;
            }

            // Check silk touch
            if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                return;
            }

            // Apply probability
            if (Math.random() > sandSmelt.probability()) {
                return;
            }

            // Prevent normal drops and drop glass instead
            e.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GLASS));
        }

        private static boolean isSandBlock(Block block, SandSmelt sandSmelt) {
            return sandSmelt.sandTypes().contains(block.getType());
        }
    }
}

