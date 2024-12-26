package zone.vao.nexoAddon.classes.bedrockbreak;

import com.nexomc.nexo.api.NexoItems;
import zone.vao.nexoAddon.utils.breaker.ModernBreakerManager;
import zone.vao.nexoAddon.utils.breaker.ModernBreakerManager.HardnessModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class BedrockBreakMechanicManager implements Listener {
    private final BedrockBreakMechanicFactory factory;

    public BedrockBreakMechanicManager(BedrockBreakMechanicFactory factory) {
        this.factory = factory;
        registerBreakModifier();
    }

    private void registerBreakModifier() {
        ModernBreakerManager.MODIFIERS.add(new HardnessModifier() {
            @Override
            public boolean isTriggered(Player player, Block block, ItemStack tool) {
                if (block.getType() != Material.BEDROCK) return false;

                String itemID = NexoItems.idFromItem(tool);
                boolean validLayer = !factory.isDisabledOnFirstLayer() || 
                                   block.getY() > block.getWorld().getMinHeight();
                                   
                return !factory.isNotImplementedIn(itemID) && validLayer;
            }

            @Override
            public void breakBlock(Player player, Block block, ItemStack tool) {
                String itemID = NexoItems.idFromItem(tool);
                BedrockBreakMechanic mechanic = (BedrockBreakMechanic) factory.getMechanic(itemID);
                if (mechanic == null) return;

                World world = block.getWorld();
                Location loc = block.getLocation();

                if (mechanic.bernouilliTest()) {
                    world.dropItemNaturally(loc, new ItemStack(Material.BEDROCK));
                }

                block.breakNaturally(tool);
            }

            @Override
            public long getPeriod(Player player, Block block, ItemStack tool) {
                String itemID = NexoItems.idFromItem(tool);
                BedrockBreakMechanic mechanic = (BedrockBreakMechanic) factory.getMechanic(itemID);
                return mechanic.getPeriod();
            }
        });
    }
}