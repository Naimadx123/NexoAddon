package zone.vao.nexoAddon.events.playerInteracts;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.mechanics.furniture.FurnitureHelpers;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.NexoAddon;

public class AdventureGamemodeSupport {

  public static void onInteract(PlayerInteractEvent event) {

    String itemId = NexoItems.idFromItem(event.getPlayer().getInventory().getItemInMainHand());
    if(event.getAction() != Action.RIGHT_CLICK_BLOCK || itemId == null || event.getHand() != EquipmentSlot.HAND || event.getPlayer().getGameMode() != GameMode.ADVENTURE || !NexoAddon.getInstance().getGlobalConfig().getBoolean("adventure_support", false)) return;
    if(event.getClickedBlock() == null || !ProtectionLib.canBuild(event.getPlayer(), event.getClickedBlock().getRelative(event.getBlockFace()).getLocation())) return;
    FurnitureMechanic furnitureMechanic = NexoFurniture.furnitureMechanic(itemId);
    if(furnitureMechanic == null) return;

    Rotation rotation = getRotation((double) event.getPlayer().getEyeLocation().getYaw(), furnitureMechanic);
    float yaw = FurnitureHelpers.INSTANCE.correctedYaw(furnitureMechanic, FurnitureHelpers.rotationToYaw(rotation));
    furnitureMechanic.place(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), yaw, event.getBlockFace(), true);
  }

  private static Rotation getRotation(Double yaw, FurnitureMechanic mechanic) {
    FurnitureMechanic.RestrictedRotation restrictedRotation = mechanic.getRestrictedRotation();
    int id = ((int) (((Location.normalizeYaw(yaw.floatValue()) + 180) * 8 / 360) + 0.5)) % 8;
    int offset = (restrictedRotation == FurnitureMechanic.RestrictedRotation.STRICT) ? 0 : 1;
    if (restrictedRotation != FurnitureMechanic.RestrictedRotation.NONE && id % 2 != 0) id -= offset;
    return Rotation.values()[id];
  }
}
