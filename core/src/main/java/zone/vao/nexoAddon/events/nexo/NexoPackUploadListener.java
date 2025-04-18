package zone.vao.nexoAddon.events.nexo;

import com.nexomc.nexo.api.events.resourcepack.NexoPackUploadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;
import zone.vao.nexoAddon.utils.RecipesUtil;

public class NexoPackUploadListener implements Listener {

  @EventHandler
  public void onPostUpload(NexoPackUploadEvent event) {

    new BukkitRunnable() {
      @Override
      public void run() {
        RecipeManager.clearRegisteredRecipes();
        RecipesUtil.loadRecipes();
      }
    }.runTaskLater(NexoAddon.getInstance(), 20L*20);
  }
}
