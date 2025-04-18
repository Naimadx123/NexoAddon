package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

public record KillMessage(String deathMessage) {

  public static class KillMessageListener implements Listener {
    @EventHandler
    public void on(PlayerDeathEvent event) {
      LivingEntity killer = event.getEntity().getKiller();
      if (killer != null) {
        ItemStack weapon = killer.getEquipment().getItemInMainHand();
        String nexoItemId = NexoItems.idFromItem(weapon);
        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
        if (mechanics == null || mechanics.getKillMessage() == null) return;

        String deathMessage = mechanics.getKillMessage().deathMessage();
        deathMessage = deathMessage.replace("<player>", event.getEntity().getName());
        deathMessage = deathMessage.replace("<killer>", killer.getName());
        event.deathMessage(MiniMessage.miniMessage().deserialize(deathMessage));
      }
    }
  }
}
