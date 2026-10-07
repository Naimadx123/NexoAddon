package zone.vao.nexoAddon.events;

import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.populators.orePopulator.CustomOrePopulator;

import java.util.ArrayList;
import java.util.List;

public class WorldLoadListener implements Listener {

  @EventHandler
  public void onWorldInit(WorldInitEvent event) {

    applyPopulator(event);
  }

  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {

    applyPopulator(event);
  }

  private void applyPopulator(WorldEvent event) {
    Bukkit.getGlobalRegionScheduler().runDelayed(NexoAddon.getInstance(), task -> {
      NexoAddon.getInstance().addBiomePopulators(event.getWorld());
      NexoAddon.getInstance().getOrePopulator().getOres().forEach(ore -> {
        if (ore.getNexoFurniture() != null || (
            ore.nexoBlocks != null &&
                NexoBlocks.isNexoStringBlock(ore.nexoBlocks.getItemID()) &&
                NexoBlocks.stringMechanic(ore.nexoBlocks.getItemID()).isSapling())
        ) return;

        List<String> worldNames = ore.getWorldNames();
        if (!worldNames.contains("all") && !worldNames.contains(event.getWorld().getName())) return;

        CustomOrePopulator customOrePopulator = new CustomOrePopulator(NexoAddon.getInstance().getOrePopulator());

        String eventWorldName = event.getWorld().getName();
        NexoAddon addon = NexoAddon.getInstance();

        addon.getWorldPopulators().computeIfAbsent(eventWorldName, k -> new ArrayList<>());
        addon.addPopulatorToWorld(event.getWorld(), customOrePopulator);
        addon.getWorldPopulators().get(eventWorldName).add(customOrePopulator);
        addon.logPopulatorAdded("BlockPopulator", ore.getId(), event.getWorld());
      });
    }, 20L * 5);
  }


  @EventHandler
  public void onWorldLoad(WorldUnloadEvent event) {

    NexoAddon.getInstance().removePopulators(event.getWorld());

  }
}
