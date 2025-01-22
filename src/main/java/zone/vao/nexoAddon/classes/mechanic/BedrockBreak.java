package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;

@Getter
public class BedrockBreak {
  private final int hardness;
  private final double probability;
  private final int durabilityCost;
  private final boolean disableOnFirstLayer;

  public BedrockBreak(final int hardness, final double probability, final int durabilityCost, final boolean disableOnFirstLayer) {
    this.hardness = hardness;
    this.probability = probability;
    this.durabilityCost = durabilityCost;
    this.disableOnFirstLayer = disableOnFirstLayer;
  }
}
