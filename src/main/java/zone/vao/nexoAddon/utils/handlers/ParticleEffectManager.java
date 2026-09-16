package zone.vao.nexoAddon.utils.handlers;

import com.nexomc.nexo.api.NexoItems;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.objecthunter.exp4j.Expression;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.items.mechanics.Aura;

public class ParticleEffectManager {

  private final NexoAddon plugin = NexoAddon.getInstance();
  private final long startMillis = System.currentTimeMillis();
  private WrappedTask task;

  public void startAuraEffectTask() {
    if(task != null && !task.isCancelled()) return;
    task = NexoAddon.instance.foliaLib.getScheduler().runTimerAsync(() -> {
      for (Player player : Bukkit.getOnlinePlayers()) {
        applyAuraEffects(player);
      }
    }, 0L, NexoAddon.getInstance().getGlobalConfig().getLong("aura_mechanic_delay", 5));
  }

  public void stopAuraEffectTask() {
    if(task != null && !task.isCancelled())
      task.cancel();
  }

  private void applyAuraEffects(Player player) {
    applyAuras(player, player.getInventory().getItemInMainHand());
    for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
      applyAuras(player, armorPiece);
    }
  }

  private void applyAuras(Player player, ItemStack item) {
    if (item == null || item.getType().isAir()) return;

    String itemId = NexoItems.idFromItem(item);
    if (itemId == null) return;

    Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
    if (mechanics == null || mechanics.getAura() == null) return;

    for (Aura aura : mechanics.getAura()) {
      applyAuraEffect(player, aura);
    }
  }

  private void applyAuraEffect(Player player, Aura aura) {
    Particle particle = aura.particle();

    if ("custom".equalsIgnoreCase(aura.type())) {
      spawnCustomParticles(player, aura);
    } else if ("simple".equalsIgnoreCase(aura.type())) {
      spawnSimpleParticles(player, particle);
    } else if ("ring".equalsIgnoreCase(aura.type())) {
      spawnRingParticles(player, particle);
    } else if ("helix".equalsIgnoreCase(aura.type())) {
      spawnHelixParticles(player, particle);
    } else if ("heart".equalsIgnoreCase(aura.type())) {
      spawnHeartParticles(player, particle);
    }
  }

  private void spawnCustomParticles(Player player, Aura aura) {
    Location location = player.getLocation();
    int points = aura.points();
    double angle = 0.0;
    double angle2 = -Math.PI / 2;

    for (Expression expression : aura.custom()) {
      expression.setVariable("x", location.getX())
          .setVariable("y", location.getY())
          .setVariable("z", location.getZ())
          .setVariable("yaw", location.getYaw())
          .setVariable("pitch", location.getPitch())
          .setVariable("time", (System.currentTimeMillis() - startMillis) / 1000.0)
          .setVariable("Math_PI", Math.PI);
    }

    for (int i = 0; i < points; i++) {
      for (int j = 0; j < points; j++) {
        double[] pos = new double[3];
        for (int k = 0; k < 3; k++) {
          pos[k] = aura.custom()[k].setVariable("angle", angle).setVariable("angle2", angle2).evaluate();
        }
        player.getWorld().spawnParticle(aura.particle(), pos[0], pos[1], pos[2], 1, 0, 0, 0, 0);

        angle += Math.PI * 2 / points;
      }
      angle2 += Math.PI / points;
    }
  }

  private void spawnHeartParticles(Player player, Particle particle) {
    int particlesCount = 100;
    double angle = 0.0;

    double x = player.getLocation().getX();
    double y = player.getLocation().getY();
    double z = player.getLocation().getZ();
    float yaw = player.getLocation().getYaw();

    double yawRadians = Math.toRadians(yaw);

    for (int i = 0; i < particlesCount; i++) {
      double heartX = 4 * Math.pow(Math.sin(angle), 3);
      double heartZ = 3 * Math.cos(angle) - 1.25 * Math.cos(2 * angle) - 0.75 * Math.cos(3 * angle) - 0.25 * Math.cos(4 * angle);

      double rotatedX = x + heartX * Math.cos(yawRadians) - heartZ * Math.sin(yawRadians);
      double rotatedZ = z + heartX * Math.sin(yawRadians) + heartZ * Math.cos(yawRadians);

      player.getWorld().spawnParticle(particle, rotatedX, y, rotatedZ, 1, 0, 0, 0, 0);

      angle += 0.1;
    }
  }


  private void spawnSimpleParticles(Player player, Particle particle) {
    player.getWorld().spawnParticle(particle, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.01);
  }

  private void spawnRingParticles(Player player, Particle particle) {
    double radius = 2.0;
    int points = 20;

    for (int i = 0; i < points; i++) {
      double angle = 2 * Math.PI * i / points;
      double x = player.getLocation().getX() + radius * Math.cos(angle);
      double z = player.getLocation().getZ() + radius * Math.sin(angle);
      double y = player.getLocation().getY();

      player.getWorld().spawnParticle(particle, x, y, z, 0, 0, 0, 0);
    }
  }


  private void spawnHelixParticles(Player player, Particle particle) {
    double radius = 1.5;
    double height = 3.0;
    double turns = 2.0;
    int points = 50;

    for (int i = 0; i < points; i++) {
      double angle = 2 * Math.PI * turns * i / points;
      double x = player.getLocation().getX() + radius * Math.cos(angle);
      double z = player.getLocation().getZ() + radius * Math.sin(angle);
      double y = player.getLocation().getY() + height * i / points;

      player.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    }
  }
}
