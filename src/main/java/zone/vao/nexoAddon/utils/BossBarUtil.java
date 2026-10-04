package zone.vao.nexoAddon.utils;

import org.bukkit.entity.Player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import zone.vao.nexoAddon.NexoAddon;

public class BossBarUtil {

  private final BossBar bossBar;
  private static BossBar.Color COLOR = BossBar.Color.WHITE;
  private static BossBar.Overlay OVERLAY = BossBar.Overlay.PROGRESS;

  public BossBarUtil(Component message) {
    bossBar = BossBar.bossBar(message, 1.0f, COLOR, OVERLAY);
  }

  public static void setupConfig() {
    String configValue = null;
    try {
      configValue = NexoAddon.getInstance().getGlobalConfig().getString("boss_bar.color", "WHITE");
      COLOR = BossBar.Color.valueOf(configValue);
    } catch (IllegalArgumentException ex) {
      NexoAddon.getInstance().getLogger()
          .warning(
              String.format("NexoAddon: The color \"%s\" is not valid for the config in boss_bar.color", configValue));
    }

    try {
      configValue = NexoAddon.getInstance().getGlobalConfig().getString("boss_bar.overlay", "PROGRESS");
      OVERLAY = BossBar.Overlay.valueOf(configValue);
    } catch (IllegalArgumentException ex) {
      NexoAddon.getInstance().getLogger()
          .warning(String.format("NexoAddon: The overlay \"%s\" is not valid for the config in boss_bar.overlay",
              configValue));
    }
  }

  public void sendToPlayer(Player player) {
    player.showBossBar(bossBar);
  }

  public void removeFromPlayer(Player player) {
    player.hideBossBar(bossBar);
  }

  public void setProgress(float progress) {
    bossBar.progress(progress);
  }

  public void setMessage(Component message) {
    bossBar.name(message);
  }

  public void removeBar() {
    bossBar.viewers().forEach(viewer -> {
      if (viewer instanceof Audience audience) {
        bossBar.removeViewer(audience);
      }
    });
  }
}
