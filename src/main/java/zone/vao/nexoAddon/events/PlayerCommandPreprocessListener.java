package zone.vao.nexoAddon.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import zone.vao.nexoAddon.utils.RecipesUtil;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

public class PlayerCommandPreprocessListener implements Listener {

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    String command = event.getMessage().toLowerCase();
    
    if((!player.hasPermission("nexo.command.reload")) && !player.isOp()) return;

    boolean isNexoReload = command.startsWith("/nexo ") || command.startsWith("/n ");
    if (!isNexoReload) return;

    String subCommand = command.replaceFirst("^/(nexo|n|nx)\\s+", "");
    
    if (subCommand.equals("rl recipes")
        || subCommand.equals("reload recipes")
        || subCommand.equals("rl all")
        || subCommand.equals("reload all")
        || subCommand.equals("rl")
        || subCommand.equals("reload")
    ) {
      RecipeManager.clearRegisteredRecipes();
      RecipesUtil.loadRecipes();
    }
  }
}
