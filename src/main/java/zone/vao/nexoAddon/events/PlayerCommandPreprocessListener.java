package zone.vao.nexoAddon.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import zone.vao.nexoAddon.utils.RecipesUtil;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

import java.util.Set;

public class PlayerCommandPreprocessListener implements Listener {

  private static final Set<String> RELOAD_COMMANDS = Set.of(
      "rl recipes", "reload recipes",
      "rl all", "reload all",
      "rl", "reload"
  );

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    String command = event.getMessage().toLowerCase();

    if (!player.hasPermission("nexo.command.reload") && !player.isOp()) return;

    if (command.startsWith("/nexo ") || command.startsWith("/n ") || command.startsWith("/nx ")) {
      String subCommand = command.replaceFirst("^/(nexo|n|nx)\\s+", "");

      if (RELOAD_COMMANDS.contains(subCommand)) {
        RecipeManager.clearRegisteredRecipes();
        RecipesUtil.loadRecipes();
      }
    }
  }
}
