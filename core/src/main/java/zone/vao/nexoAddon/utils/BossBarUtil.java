package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtil {

  private final BossBar bossBar;

  public BossBarUtil(String message, BarColor color, BarStyle style) {
    bossBar = Bukkit.createBossBar(message, color, style);
  }

  public void sendToPlayer(Player player) {
    bossBar.addPlayer(player);
  }

  public void removeFromPlayer(Player player) {
    bossBar.removePlayer(player);
  }

  public void setProgress(double progress) {
    bossBar.setProgress(progress);
  }

  public void setMessage(String message) {
    bossBar.setTitle(message);
  }

  public void removeBar() {
    bossBar.removeAll();
  }
}

