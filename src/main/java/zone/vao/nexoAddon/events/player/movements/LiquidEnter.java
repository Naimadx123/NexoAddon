package zone.vao.nexoAddon.events.player.movements;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.Liquid;
import zone.vao.nexoAddon.utils.CommandUtil;
import zone.vao.nexoAddon.utils.CooldownUtil;
import zone.vao.nexoAddon.utils.LiquidUtil;

import java.util.UUID;

public class LiquidEnter {

  public static void onLiquidEnter(PlayerMoveEvent event) {
    if (!NexoAddon.getInstance().getIsLiquid()) return;
    if (!event.hasChangedBlock()) return;
    if (event.isCancelled()) return;

    Block block = event.getTo().getBlock();
    if (!LiquidUtil.isWater(block)) return;

    Liquid liquid = LiquidUtil.byBiome(block.getBiome());
    if (liquid == null) return;

    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    if (!liquid.effects().isEmpty()
        && CooldownUtil.tryConsume(liquid.effectsPool(), uuid, liquid.effectsCooldown())) {
      for (PotionEffect effect : liquid.effects()) player.addPotionEffect(effect);
    }

    if (!liquid.commands().isEmpty()
        && CooldownUtil.tryConsume(liquid.commandsPool(), uuid, liquid.commandsCooldown())) {
      CommandUtil.dispatch(player, liquid.commands(), liquid.commandsAsConsole());
    }
  }
}
