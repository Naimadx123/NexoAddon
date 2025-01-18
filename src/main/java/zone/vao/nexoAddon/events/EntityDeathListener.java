package zone.vao.nexoAddon.events;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;

public class EntityDeathListener implements Listener {
    @EventHandler
    public void on(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            String nexoItemId = NexoItems.idFromItem(weapon);
            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
            if (mechanics == null || mechanics.getKillMessage() == null) return;

            String deathMessage = mechanics.getKillMessage().getDeathMessage();
            deathMessage = deathMessage.replace("<player>", event.getEntity().getName());
            deathMessage = deathMessage.replace("<killer>", killer.getName());
            event.deathMessage(MiniMessage.miniMessage().deserialize(deathMessage));
        }
    }
}
