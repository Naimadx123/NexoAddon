package zone.vao.nexoAddon.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;
import zone.vao.nexoAddon.utils.RecipesUtil;

public class PlayerCommandPreprocessListener implements Listener {

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    String command = event.getMessage().toLowerCase();

    if((!player.hasPermission("nexo.command.reload")) && !player.isOp()) return;

    if (command.equals("/nexo rl recipes")
        || command.equals("/nexo reload recipes")
        || command.equals("/n rl recipes")
        || command.equals("/n reload recipes")
        || command.equals("/n rl all")
        || command.equals("/n reload all")
        || command.equals("/nexo rl all")
        || command.equals("/nexo reload all")
        || command.equals("/n rl")
        || command.equals("/n reload")
        || command.equals("/nexo rl")
        || command.equals("/nexo reload")
    ) {
      RecipeManager.clearRegisteredRecipes();
      RecipesUtil.loadRecipes();
    }
  }
}
