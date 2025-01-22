package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;
import zone.vao.nexoAddon.NexoAddon;

@Getter
public class BigMining {
  private final int radius;
  private final int depth;
  private final boolean switchable;

  public BigMining(final int radius, final int depth, final boolean switchable) {
    this.radius = radius;
    this.depth = depth;
    this.switchable = switchable;
  }

  public static boolean isBigMiningTool(String toolId) {
    return toolId != null
        && NexoAddon.getInstance().getMechanics().containsKey(toolId)
        && NexoAddon.getInstance().getMechanics().get(toolId).getBigMining() != null;
  }
}
