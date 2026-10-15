package zone.vao.nexoAddon.populators;

import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public record PopulatorItem(String id, BlockData data, BlockData upperData, FurnitureMechanic furniture, Integer saplingTime) {

  public boolean requiresWorld() {
    return furniture != null || saplingTime != null;
  }

  public BlockData blockData(BlockFace facing) {
    BlockData result = data.clone();
    if (result instanceof Directional directional && directional.getFaces().contains(facing))
      directional.setFacing(facing);
    return result;
  }
}
