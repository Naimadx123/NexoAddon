package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;
import org.bukkit.Particle;

@Getter
public class Aura {
  private final Particle particle;
  private final String type;
  private final String formula;

  public Aura(final Particle particle, final String type, final String formula) {
    this.particle = particle;
    this.type = type;
    this.formula = formula;
  }
}
