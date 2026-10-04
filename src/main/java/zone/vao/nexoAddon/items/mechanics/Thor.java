package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.protectionlib.ProtectionLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public record Thor(int lightningBoltsAmount, double randomLocationVariation, int delay) {

  private static final int TARGET_DISTANCE = 50;

  public Location randomizedLocation(Location origin) {
    if (randomLocationVariation <= 0) return origin;

    ThreadLocalRandom random = ThreadLocalRandom.current();
    double half = randomLocationVariation / 2;
    return origin.clone().add(
        random.nextDouble(randomLocationVariation) - half,
        0,
        random.nextDouble(randomLocationVariation) - half
    );
  }

  public static class ThorListener implements Listener {

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onThor(PlayerInteractEvent event) {
      if (!canUseThor(event)) return;

      Player player = event.getPlayer();
      Thor thor = getThor(player);
      if (thor == null) return;

      Block target = player.getTargetBlockExact(TARGET_DISTANCE);
      if (target == null) return;

      Location location = target.getLocation();
      World world = location.getWorld();
      if (world == null || !ProtectionLib.canInteract(player, location)) return;

      if (thor.delay() > 0 && !consumeCooldown(player, thor.delay())) return;

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocation(location, r -> {
        for (int i = 0; i < thor.lightningBoltsAmount(); i++) {
          world.strikeLightning(thor.randomizedLocation(location));
        }
      });
    }

    private boolean canUseThor(PlayerInteractEvent event) {
      Action action = event.getAction();
      if (event.getHand() != EquipmentSlot.HAND
          || action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return false;
      if (event.useItemInHand() == Event.Result.DENY) return false;

      Block clicked = event.getClickedBlock();
      return clicked == null || !clicked.getType().isInteractable() || event.useInteractedBlock() == Event.Result.DENY;
    }

    private Thor getThor(Player player) {
      if (NexoAddon.getInstance().getMechanics().isEmpty()) return null;

      String itemId = NexoItems.idFromItem(player.getInventory().getItemInMainHand());
      if (itemId == null || itemId.isEmpty()) return null;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
      return mechanics == null ? null : mechanics.getThor();
    }

    private boolean consumeCooldown(Player player, int delay) {
      long now = System.currentTimeMillis();
      Long readyAt = cooldowns.get(player.getUniqueId());

      if (readyAt != null && readyAt > now) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
            NexoAddon.getInstance().getGlobalConfig()
                .getString("messages.thor.cooldown", "<red>You must wait <time>s before calling the storm again.")
                .replace("<time>", String.format("%.1f", (readyAt - now) / 1000.0))));
        return false;
      }

      cooldowns.put(player.getUniqueId(), now + delay);
      return true;
    }
  }
}
