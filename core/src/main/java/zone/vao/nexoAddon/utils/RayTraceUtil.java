package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;


public class RayTraceUtil {

  public static FurnitureMechanic ray(Player player){

    RayTraceResult result = player.getWorld().rayTrace(
        player.getEyeLocation(),
        player.getEyeLocation().getDirection(),
        5,
        FluidCollisionMode.NEVER,
        true,
        0.5,
        NexoFurniture::isFurniture
    );
    if (result == null || result.getHitEntity() == null) return null;

    return NexoFurniture.furnitureMechanic(result.getHitEntity());
  }
}
