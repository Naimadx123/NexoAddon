package zone.vao.nexoAddon.events;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.ItemConfigUtil;

public class NexoItemsLoadedListener implements Listener {


  @EventHandler
  public void on(NexoItemsLoadedEvent event) {
    NexoAddon.getInstance().getNexoFiles().clear();
    NexoAddon.getInstance().getNexoFiles().addAll(ItemConfigUtil.getItemFiles());

    ItemConfigUtil.loadComponents();
    ItemConfigUtil.loadMechanics();

    NexoAddon.getInstance().getParticleEffectManager().stopAuraEffectTask();
    new BukkitRunnable() {
      @Override
      public void run(){
        NexoAddon.getInstance().getParticleEffectManager().startAuraEffectTask();
      }
    }.runTaskLater(NexoAddon.getInstance(), 2L);
  }
}
