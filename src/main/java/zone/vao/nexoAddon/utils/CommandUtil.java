package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandUtil {

  public static void dispatch(Player player, List<String> commands, boolean asConsole) {
    if (player == null || commands == null || commands.isEmpty()) return;

    UUID uuid = player.getUniqueId();
    Location location = player.getLocation();
    World world = location.getWorld();

    List<String> resolved = new ArrayList<>(commands.size());
    for (String raw : commands) {
      if (raw == null || raw.isBlank()) continue;

      String command = raw
          .replace("<player>", player.getName())
          .replace("<uuid>", uuid.toString())
          .replace("<world>", world == null ? "" : world.getName())
          .replace("<x>", String.valueOf(location.getBlockX()))
          .replace("<y>", String.valueOf(location.getBlockY()))
          .replace("<z>", String.valueOf(location.getBlockZ()));

      if (command.startsWith("/")) command = command.substring(1);
      if (!command.isBlank()) resolved.add(command);
    }
    if (resolved.isEmpty()) return;

    NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityLater(player, task -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online == null || !online.isOnline()) return;

      for (String command : resolved) {
        try {
          if (asConsole) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
          else online.performCommand(command);
        } catch (Exception exception) {
          NexoAddon.getInstance().getLogger().warning("Failed to run command `" + command + "`: " + exception.getMessage());
        }
      }
    }, 1L);
  }
}
