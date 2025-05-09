package zone.vao.nexoAddon.events.nexo;

import com.nexomc.nexo.api.events.resourcepack.NexoPackUploadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.RecipesUtil;
import zone.vao.nexoAddon.utils.handlers.RecipeManager;

public class NexoPackUploadListener implements Listener {

  @EventHandler
  public void onPostUpload(NexoPackUploadEvent event) {
    NexoAddon.instance.foliaLib.getScheduler().runLater(() -> {
      RecipeManager.clearRegisteredRecipes();
      RecipesUtil.loadRecipes();
    }, 20L*20);
  }
}
