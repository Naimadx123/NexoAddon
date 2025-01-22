package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;

@Getter
public class Repair {
  private final double ratio;
  private final int fixedAmount;

  public Repair(final double ratio, final int fixedAmount) {
    this.ratio = ratio;
    this.fixedAmount = fixedAmount;
  }
}
