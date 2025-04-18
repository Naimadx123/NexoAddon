package zone.vao.nexoAddon.utils;

import org.bukkit.Particle;

public class ParticleUtil {

  public static Particle getHappyVillagerParticle() {
    try {
      return Particle.valueOf("HAPPY_VILLAGER");
    } catch (IllegalArgumentException e) {
      return Particle.valueOf("VILLAGER_HAPPY");
    }
  }
}
