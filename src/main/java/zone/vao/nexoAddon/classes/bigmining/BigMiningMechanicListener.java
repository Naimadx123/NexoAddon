package zone.vao.nexoAddon.classes.bigmining;

import com.nexomc.nexo.utils.BlockHelpers;
import zone.vao.nexoAddon.utils.EventUtils;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import com.nexomc.nexo.mechanics.MechanicsManager;
import java.util.List;
import java.util.Set;

public class BigMiningMechanicListener implements Listener {
    private final BigMiningMechanicFactory factory;
    private int blocksToProcess = 0;
    private static final Set<Material> UNBREAKABLE_BLOCKS = Set.of(Material.BEDROCK, Material.BARRIER);

    public BigMiningMechanicListener(final BigMiningMechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();

        if (blocksToProcess > 0) {
            blocksToProcess -= 1;
            return;
        }

        final List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 5);
        final BigMiningMechanic mechanic = (BigMiningMechanic) factory.getMechanic(item);
        if (mechanic == null || lastTwoTargetBlocks.size() < 2) return;

        final Block nearestBlock = lastTwoTargetBlocks.get(0);
        final Block secondBlock = lastTwoTargetBlocks.get(1);
        final BlockFace blockFace = secondBlock.getFace(nearestBlock);
        final Location secondMinusNearest = secondBlock.getLocation().subtract(nearestBlock.getLocation());
        final int modifier = secondMinusNearest.getBlockX() + secondMinusNearest.getBlockY() + secondMinusNearest.getBlockZ();

        final Location initialLocation = event.getBlock().getLocation();

        Location tempLocation;
        for (double relativeX = -mechanic.getRadius(); relativeX <= mechanic.getRadius(); relativeX++)
            for (double relativeY = -mechanic.getRadius(); relativeY <= mechanic.getRadius(); relativeY++)
                for (double relativeDepth = 0; relativeDepth < mechanic.getDepth(); relativeDepth++) {
                    tempLocation = transpose(initialLocation, blockFace, relativeX, relativeY,
                            relativeDepth * modifier);
                    if (tempLocation.equals(initialLocation))
                        continue;
                    breakBlock(player, tempLocation.getBlock(), item);
                }
        blocksToProcess = 0;
    }
    private void breakBlock(final Player player, final Block block, final ItemStack itemStack) {
        if (block.isLiquid()
                || UNBREAKABLE_BLOCKS.contains(block.getType())
                || !ProtectionLib.canBreak(player, block.getLocation()))
            return;
        blocksToProcess += 1;
        
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        if (!factory.callEvents() || !EventUtils.handleEvent(event)) return;
        
        if (event.isDropItems()) {
            block.breakNaturally(itemStack);
        } else {
            block.setType(Material.AIR);
        }
    }

    private Location transpose(Location loc, final BlockFace face, final double relX, final double relY,
                             final double relativeDepth) {
        loc = loc.clone();
        return switch (face) {
            case WEST, EAST -> loc.add(relativeDepth, relX, relY);
            case UP, DOWN -> loc.add(relX, relativeDepth, relY);
            default -> loc.add(relX, relY, relativeDepth);
        };
    }
}