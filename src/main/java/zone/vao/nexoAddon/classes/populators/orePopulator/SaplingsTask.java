package zone.vao.nexoAddon.classes.populators.orePopulator;

import com.jeff_media.customblockdata.CustomBlockData;
import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.api.NexoBlocks;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SaplingsTask extends BukkitRunnable {

  public final static Map<Location, String> placedSaplings = new HashMap<>();

  @Override
  public void run() {
    if(!placedSaplings.isEmpty()) {
      Iterator<Map.Entry<Location, String>> iterator = placedSaplings.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<Location, String> entry = iterator.next();
        Location placedSapling = entry.getKey().clone();
        String itemId = entry.getValue();
        if(!placedSapling.getChunk().isLoaded()) continue;
        PersistentDataContainer pdc = new CustomBlockData(placedSapling.getBlock(), NexoPlugin.instance());
        pdc.set(new NamespacedKey(NexoPlugin.instance(), "sapling"), PersistentDataType.INTEGER, NexoBlocks.stringMechanic(itemId).sapling().getNaturalGrowthTime());
        iterator.remove();
      }
    }
  }
}
