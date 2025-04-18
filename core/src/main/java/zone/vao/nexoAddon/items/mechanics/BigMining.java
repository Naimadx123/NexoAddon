package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.BlockUtil;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.List;
import java.util.Set;

public record BigMining(int radius, int depth, boolean switchable, List<Material> materials) {

  public static boolean isBigMiningTool(String toolId) {
    return toolId != null && NexoAddon.getInstance().getMechanics().containsKey(toolId) && NexoAddon.getInstance().getMechanics().get(toolId).getBigMining() != null;
  }

  public static class BigMiningListener implements Listener {
    private static int activeBlockBreaks = 0;

    @EventHandler
    public static void onBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();

      String toolId = NexoItems.idFromItem(tool);
      if (!BigMining.isBigMiningTool(toolId)) return;

      if (activeBlockBreaks > 0) {
        activeBlockBreaks--;
        return;
      }

      List<Block> targetBlocks = player.getLastTwoTargetBlocks(Set.of(Material.AIR, Material.WATER, Material.LAVA, Material.LADDER), 5);
      if (targetBlocks.size() < 2) return;

      BigMining bigMiningMechanic = NexoAddon.getInstance()
          .getMechanics()
          .get(toolId)
          .getBigMining();
      if (bigMiningMechanic == null) return;

      PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();

      if(bigMiningMechanic.switchable()
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
      double radius = mechanic.radius();
      double depth = mechanic.depth();

      for (double xOffset = -radius; xOffset <= radius; xOffset++) {
        for (double yOffset = -radius; yOffset <= radius; yOffset++) {
          for (double zOffset = 0; zOffset < depth; zOffset++) {
            tempLocation = calculateTargetLocation(origin, face, xOffset, yOffset, zOffset * modifier);

            if (tempLocation.equals(origin)) continue;

            attemptBlockBreak(player, tempLocation.getBlock(), tool, mechanic);
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

    private static void attemptBlockBreak(Player player, Block block, ItemStack tool, BigMining mechanic) {
      if (isUnbreakableBlock(player, block)) return;

      activeBlockBreaks++;
      BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);

      if (!EventUtil.callEvent(blockBreakEvent) || !mechanic.materials().isEmpty() && !mechanic.materials().contains(block.getType())) return;

      if (blockBreakEvent.isDropItems()) {
        block.breakNaturally(tool, true, true);
      } else {
        block.setType(Material.AIR);
      }
    }

    private static boolean isUnbreakableBlock(Player player, Block block) {
      return block.isLiquid()
          || BlockUtil.UNBREAKABLE_BLOCKS.contains(block.getType())
          || !ProtectionLib.canBreak(player, block.getLocation());
    }

    private final static NamespacedKey key = new NamespacedKey(NexoAddon.getInstance(), "bigMiningSwitchable");

    @EventHandler
    public static void onToggle(final PlayerInteractEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();

      String toolId = NexoItems.idFromItem(tool);
      if (!BigMining.isBigMiningTool(toolId) || event.getHand() != EquipmentSlot.HAND) return;
      if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

      BigMining bigMiningMechanic = NexoAddon.getInstance()
          .getMechanics()
          .get(toolId)
          .getBigMining();

      if (!bigMiningMechanic.switchable() || tool.getItemMeta() == null) return;

      var meta = tool.getItemMeta();
      PersistentDataContainer pdc = meta.getPersistentDataContainer();

      if (!pdc.has(key, PersistentDataType.BOOLEAN)) {
        pdc.set(key, PersistentDataType.BOOLEAN, true);
        tool.setItemMeta(meta);
        turnOn(player, pdc);
        return;
      }

      boolean isOn = pdc.get(key, PersistentDataType.BOOLEAN);
      if (isOn) {
        turnOff(player, pdc);
      } else {
        turnOn(player, pdc);
      }

      tool.setItemMeta(meta);
    }

    private static void turnOff(final Player player, PersistentDataContainer pdc) {
      pdc.set(key, PersistentDataType.BOOLEAN, false);
      Audience.audience(player)
          .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig().getString("messages.bigmining.off", "<red>BigMining off")));
    }

    private static void turnOn(final Player player, PersistentDataContainer pdc) {
      pdc.set(key, PersistentDataType.BOOLEAN, true);

      Audience.audience(player)
          .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig().getString("messages.bigmining.on", "<green>BigMining on")));
    }
  }
}
