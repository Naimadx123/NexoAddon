package zone.vao.nexoAddon.events.player.movements;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
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
        bossBar.removeBar();
        NexoAddon.getInstance().getBossBars().remove(player.getUniqueId());
      }
      return;
    }
    ItemBuilder itemBuilder = NexoItems.itemFromId(fm.getItemID());
    String name = fm.getItemID();
    if(itemBuilder != null && itemBuilder.getItemName() != null)
      name = ((TextComponent) itemBuilder.getItemName()).content();

    BossBarUtil bossBar = NexoAddon.getInstance().getBossBars().get(player.getUniqueId());
    if(bossBar == null) {
      bossBar = new BossBarUtil(name, BarColor.WHITE, BarStyle.SOLID);
      NexoAddon.getInstance().getBossBars().put(player.getUniqueId(), bossBar);
      bossBar.sendToPlayer(player);
    }
    bossBar.setMessage(name);
  }
}
