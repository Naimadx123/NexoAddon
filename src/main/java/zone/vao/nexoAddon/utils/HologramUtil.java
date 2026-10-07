package zone.vao.nexoAddon.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import zone.vao.nexoAddon.NexoAddon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HologramUtil {

  private static final Map<UUID, TextDisplay> holograms = new ConcurrentHashMap<>();

  public static void displayProgressBar(Entity entity, double progress, Player player) {
    if (entity == null || progress < 0.0 || progress > 1.0) return;

    entity.getScheduler().run(NexoAddon.getInstance(), startDisplay -> {
      World world = entity.getWorld();
      Location entityLocation = entity.getLocation().clone();
      Location hologramLocation = entityLocation.add(0, 0.5, 0);

      Component progressBar = getProgressBar(progress, 10);

      if (player != null && holograms.containsKey(player.getUniqueId())) {
        TextDisplay existingHologram = holograms.remove(player.getUniqueId());
        if (existingHologram != null)
          existingHologram.getScheduler().run(NexoAddon.getInstance(), task -> existingHologram.remove(), null);
      }

      TextDisplay hologram = world.spawn(hologramLocation, TextDisplay.class, holo -> {
        holo.customName(progressBar);
        holo.setCustomNameVisible(true);
        holo.setGravity(false);
        holo.setInvisible(true);
        holo.setBillboard(Display.Billboard.CENTER);
        if (player != null) {
          holo.setVisibleByDefault(false);
          player.getScheduler().run(NexoAddon.getInstance(), task -> player.showEntity(NexoAddon.getInstance(), holo), null);
        }
      });

      if (player != null)
        holograms.put(player.getUniqueId(), hologram);

      hologram.getScheduler().runDelayed(NexoAddon.getInstance(), task -> {
        hologram.remove();
        if (player != null)
          holograms.remove(player.getUniqueId(), hologram);
      }, () -> {
        if (player != null) holograms.remove(player.getUniqueId(), hologram);
      }, 60L);
    }, null);
  }

  private static Component getProgressBar(double progress, int length) {
    int filledLength = (int) (progress * length);
    int emptyLength = length - filledLength;

    Component filledPart = Component.text("█".repeat(filledLength)).color(NamedTextColor.DARK_GREEN);
    Component emptyPart = Component.text("█".repeat(emptyLength)).color(NamedTextColor.RED);

    return filledPart.append(emptyPart);
  }

}
