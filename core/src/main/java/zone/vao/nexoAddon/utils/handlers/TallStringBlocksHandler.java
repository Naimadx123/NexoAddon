package zone.vao.nexoAddon.utils.handlers;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.nexo.utils.drops.Loot;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class TallStringBlocksHandler {

  public static boolean isStringBlock(Block block) {
    return NexoBlocks.isNexoStringBlock(block)
        || (NexoBlocks.isNexoStringBlock(block.getRelative(BlockFace.DOWN))
        && NexoBlocks.stringMechanic(block.getRelative(BlockFace.DOWN)).isTall());
  }

  public static void removeStringBlock(Block block, boolean dropLoot) {
    if (NexoBlocks.isNexoStringBlock(block)) {
      if (NexoBlocks.stringMechanic(block).isTall()) {
        removeTallStringBlock(block, dropLoot);
      } else {
        NexoBlocks.remove(block.getLocation());
      }
    } else {
      Block bottomBlock = block.getRelative(BlockFace.DOWN);
      if (NexoBlocks.isNexoStringBlock(bottomBlock) && NexoBlocks.stringMechanic(bottomBlock).isTall()) {
        removeTallStringBlock(bottomBlock, dropLoot);
      }
    }
  }

  private static void removeTallStringBlock(Block bottomBlock, boolean dropLoot) {

    if (dropLoot) {
      NexoBlocks.remove(bottomBlock.getLocation());
    } else {
      List<Loot> loots = new ArrayList<>();
      Drop drop = new Drop(loots, false, false, NexoBlocks.stringMechanic(bottomBlock).getItemID());
      NexoBlocks.remove(bottomBlock.getLocation(), null, drop);
    }
  }

  public static String getStringBlockId(Block block) {
    if (!isStringBlock(block)) return null;
    if(NexoBlocks.isNexoStringBlock(block)){
      return NexoBlocks.stringMechanic(block).getItemID();
    } else{
      return NexoBlocks.stringMechanic(block.getRelative(BlockFace.DOWN, 1)).getItemID();
    }
  }
}
