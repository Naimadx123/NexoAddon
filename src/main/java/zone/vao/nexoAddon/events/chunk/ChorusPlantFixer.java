package zone.vao.nexoAddon.events.chunk;

import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChorusPlantFixer {
  private static final BlockData CHORUS_DATA = Material.CHORUS_PLANT.createBlockData();

  public static void onLoad(ChunkLoadEvent event){
    if(!event.getWorld().getName().equalsIgnoreCase("world_end")) return;
    
    Chunk chunk = event.getChunk();
    int minHeight = event.getWorld().getMinHeight();
    int maxHeight = event.getWorld().getMaxHeight();
    
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = minHeight; y < maxHeight; y++) {
          Block block = chunk.getBlock(x, y, z);
          
          if (block.getType() == Material.CHORUS_PLANT) {
            BlockData currentData = block.getBlockData();
            if (!currentData.matches(CHORUS_DATA) && !NexoBlocks.isNexoChorusBlock(block)) {
              block.setBlockData(CHORUS_DATA, false);
            }
          }
        }
      }
    }
  }
}
