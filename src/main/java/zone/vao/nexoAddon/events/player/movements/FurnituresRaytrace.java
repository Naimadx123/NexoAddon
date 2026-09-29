package zone.vao.nexoAddon.events.player.movements;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.BossBarUtil;
import zone.vao.nexoAddon.utils.RayTraceUtil;

public class FurnituresRaytrace {

  public static void onFurnituresRaytrace(PlayerMoveEvent event){
    Player player = event.getPlayer();
    FurnitureMechanic fm = RayTraceUtil.ray(player);
    if(fm == null){
      BossBarUtil bossBar = NexoAddon.getInstance().getBossBars().get(player.getUniqueId());
      if(bossBar!=null) {
        bossBar.removeFromPlayer(player);
        NexoAddon.getInstance().getBossBars().remove(player.getUniqueId());
      }
      return;
    }
    ItemBuilder itemBuilder = NexoItems.itemFromId(fm.getItemID());
    Component name = null;
    if(itemBuilder != null && itemBuilder.getItemName() != null) {
      name = itemBuilder.getItemName();
    } else {
      name = Component.text(fm.getItemID());
    }

    BossBarUtil bossBar = NexoAddon.getInstance().getBossBars().get(player.getUniqueId());
    if(bossBar == null) {
      bossBar = new BossBarUtil(name);
      NexoAddon.getInstance().getBossBars().put(player.getUniqueId(), bossBar);
      bossBar.sendToPlayer(player);
    }
    bossBar.setMessage(name);
  }
}
