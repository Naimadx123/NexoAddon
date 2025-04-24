package zone.vao.nexoAddon.nms;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import java.util.List;

public interface INmsHandler {
  List<NamespacedKey> getDatapackCustomBiomes();
  NamespacedKey getBiomeKeyAtLocation(Location location);
}
