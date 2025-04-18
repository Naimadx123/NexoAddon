package zone.vao.nexoAddon.events.nexo;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.ItemConfigUtil;
import zone.vao.nexoAddon.utils.SkullUtil;

public class NexoItemsLoadedListener implements Listener {
  @Getter
  public boolean firstNexoItemLoaded = false;

  @EventHandler
  public void on(NexoItemsLoadedEvent event) {
    NexoAddon.getInstance().getNexoFiles().clear();
    NexoAddon.getInstance().getNexoFiles().addAll(ItemConfigUtil.getItemFiles());

    ItemConfigUtil.loadComponents();
    ItemConfigUtil.loadMechanics();

    SkullUtil.applyTextures();

    NexoAddon.getInstance().getParticleEffectManager().stopAuraEffectTask();
    new BukkitRunnable() {
      @Override
      public void run(){
        NexoAddon.getInstance().getParticleEffectManager().startAuraEffectTask();
      }
    }.runTaskLater(NexoAddon.getInstance(), 2L);

    if(!firstNexoItemLoaded) {

      NexoAddon.getInstance().initializePopulators();
      firstNexoItemLoaded = true;
    }
  }
}
