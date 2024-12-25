package zone.vao.nexoAddon.utils.breaker;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModernBreakerManager {
    public static final List<HardnessModifier> MODIFIERS = new ArrayList<>();
    
    public interface HardnessModifier {
        boolean isTriggered(Player player, Block block, ItemStack tool);
        void breakBlock(Player player, Block block, ItemStack tool);
        long getPeriod(Player player, Block block, ItemStack tool);
    }
}
