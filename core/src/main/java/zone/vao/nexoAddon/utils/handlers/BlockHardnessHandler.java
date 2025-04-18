package zone.vao.nexoAddon.utils.handlers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.BedrockBreak;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.HashMap;
import java.util.Map;

public class BlockHardnessHandler implements PacketListener {

  private final Map<Location, BukkitTask> breakingTasks = new HashMap<>();
  private final Map<Location, Integer> breakingProgress = new HashMap<>();

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {

    Player player = event.getPlayer();

    if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
    if (player.getGameMode() == GameMode.CREATIVE) return;

    WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
    Vector3i position = digging.getBlockPosition();
    DiggingAction digType = digging.getAction();
    if(digType == null) return;
    Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());

    if (digType == DiggingAction.START_DIGGING) {
      handleStartBreak(player, location, digging);
    } else if (digType == DiggingAction.FINISHED_DIGGING ||
        digType == DiggingAction.CANCELLED_DIGGING) {
      handleStopBreak(location, digging);
    }
  }

  private void handleStartBreak(Player player, Location location, WrapperPlayClientPlayerDigging digging) {
    ItemStack tool = player.getInventory().getItemInMainHand();
    String toolId = NexoItems.idFromItem(tool);

    if (toolId == null
        || NexoAddon.getInstance().getMechanics().isEmpty()
        || NexoAddon.getInstance().getMechanics().get(toolId) == null
    ) return;

    BedrockBreak bedrockBreak = NexoAddon.getInstance().getMechanics().get(toolId).getBedrockBreak();
    if (bedrockBreak == null) return;

    Block block = location.getBlock();
    if (block.getType() != Material.BEDROCK || bedrockBreak.disableOnFirstLayer() && block.getY() <= block.getWorld().getMinHeight()) return;

    int hardness = bedrockBreak.hardness();
    double probability = bedrockBreak.probability();

    BukkitScheduler scheduler = Bukkit.getScheduler();
    breakingTasks.put(location, scheduler.runTaskTimer(NexoAddon.getInstance(), new Runnable() {
      int progress = 0;

      @Override
      public void run() {
        if (!block.getType().equals(Material.BEDROCK)) {
          stopBreaking(location, digging);
          return;
        }

        int lastStage = breakingProgress.getOrDefault(location, -1);
        progress++;
        breakingProgress.put(location, progress);

        int newStage = getBreakStage(progress, hardness);
        if (newStage != lastStage) {
          sendBlockBreakAnimation(location, newStage, digging);
        }

        if (progress >= hardness) {
          stopBreaking(location, digging);
          if (EventUtil.callEvent(new BlockBreakEvent(block, player)) && ProtectionLib.canBreak(player, location)) {
            Bukkit.getScheduler().runTask(NexoAddon.getInstance(), () -> {
              if(Math.random() <= probability)
                block.getWorld().dropItemNaturally(location, new ItemStack(Material.BEDROCK));
              block.breakNaturally();

              if(tool.getItemMeta() instanceof Damageable damageable){
                damageable.setDamage(damageable.getDamage()+bedrockBreak.durabilityCost());
                int maxDurability = NexoItems.itemFromId(toolId).getDurability() != null ? NexoItems.itemFromId(toolId).getDurability() : NexoItems.itemFromId(toolId).build().getType().getMaxDurability();
                if(damageable.getDamage() >= maxDurability) {
                  player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                  block.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                  return;
                }
                player.getInventory().getItemInMainHand().setItemMeta(damageable);
              }
            });
          }
        }
      }
    }, 0L, 10L));
  }

  private int getBreakStage(double progress, double hardness) {
    double ratio = progress / hardness;
    if (ratio >= 1) return 9;
    return Math.min(9, (int) Math.ceil(9 * ratio));
  }


  private void handleStopBreak(Location location, WrapperPlayClientPlayerDigging digging) {
    stopBreaking(location, digging);
  }

  private void stopBreaking(Location location, WrapperPlayClientPlayerDigging digging) {
    BukkitTask task = breakingTasks.remove(location);
    if (task != null) {
      task.cancel();
    }
    breakingProgress.remove(location);
    sendBlockBreakAnimation(location, -1, digging);
  }

  private void sendBlockBreakAnimation(Location location, int stage, WrapperPlayClientPlayerDigging digging) {
    WrapperPlayServerBlockBreakAnimation newDigging = new WrapperPlayServerBlockBreakAnimation(location.hashCode(), digging.getBlockPosition(), (byte) stage);

    for (Player player : location.getWorld().getPlayers()) {
      try {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, newDigging);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
