package zone.vao.nexoAddon.events.blockBreaks;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.mechanic.BigMining;
import zone.vao.nexoAddon.utils.BlockUtil;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.List;

public class BigMiningListener {

  private static int activeBlockBreaks = 0;

  public static void handleBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    ItemStack tool = player.getInventory().getItemInMainHand();

    String toolId = NexoItems.idFromItem(tool);
    if (!BigMining.isBigMiningTool(toolId)) return;

    if (activeBlockBreaks > 0) {
      activeBlockBreaks--;
      return;
    }

    List<Block> targetBlocks = player.getLastTwoTargetBlocks(null, 5);
    if (targetBlocks.size() < 2) return;

    BigMining bigMiningMechanic = NexoAddon.getInstance()
        .getMechanics()
        .get(toolId)
        .getBigMining();
    if (bigMiningMechanic == null) return;

    PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();

    if(bigMiningMechanic.isSwitchable()
        && pdc.has(new NamespacedKey(NexoAddon.getInstance(), "bigMiningSwitchable"), PersistentDataType.BOOLEAN)
        && Boolean.FALSE.equals(pdc.get(new NamespacedKey(NexoAddon.getInstance(), "bigMiningSwitchable"), PersistentDataType.BOOLEAN))
    ) return;

    Block primaryBlock = targetBlocks.get(0);
    Block secondaryBlock = targetBlocks.get(1);
    BlockFace breakFace = secondaryBlock.getFace(primaryBlock);
    int directionalModifier = calculateModifier(primaryBlock, secondaryBlock);

    breakBlocksInRadius(player, event.getBlock().getLocation(), breakFace, bigMiningMechanic, directionalModifier, tool);
    activeBlockBreaks = 0;
  }

  private static int calculateModifier(Block primaryBlock, Block secondaryBlock) {
    Location delta = secondaryBlock.getLocation().subtract(primaryBlock.getLocation());
    return delta.getBlockX() + delta.getBlockY() + delta.getBlockZ();
  }

  private static void breakBlocksInRadius(Player player, Location origin, BlockFace face, BigMining mechanic, int modifier, ItemStack tool) {
    Location tempLocation;
    double radius = mechanic.getRadius();
    double depth = mechanic.getDepth();

    for (double xOffset = -radius; xOffset <= radius; xOffset++) {
      for (double yOffset = -radius; yOffset <= radius; yOffset++) {
        for (double zOffset = 0; zOffset < depth; zOffset++) {
          tempLocation = calculateTargetLocation(origin, face, xOffset, yOffset, zOffset * modifier);

          if (tempLocation.equals(origin)) continue;

          attemptBlockBreak(player, tempLocation.getBlock(), tool);
        }
      }
    }
  }

  private static Location calculateTargetLocation(Location origin, BlockFace face, double xOffset, double yOffset, double zOffset) {
    Location target = origin.clone();
    return switch (face) {
      case WEST, EAST -> target.add(zOffset, xOffset, yOffset);
      case UP, DOWN -> target.add(xOffset, zOffset, yOffset);
      default -> target.add(xOffset, yOffset, zOffset);
    };
  }

  private static void attemptBlockBreak(Player player, Block block, ItemStack tool) {
    if (isUnbreakableBlock(player, block)) return;

    activeBlockBreaks++;
    BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);

    if (!EventUtil.callEvent(blockBreakEvent)) return;

    if (blockBreakEvent.isDropItems()) {
      block.breakNaturally(tool);
    } else {
      block.setType(Material.AIR);
    }
  }

  private static boolean isUnbreakableBlock(Player player, Block block) {
    return block.isLiquid()
        || BlockUtil.UNBREAKABLE_BLOCKS.contains(block.getType())
        || !ProtectionLib.canBreak(player, block.getLocation());
  }
}
