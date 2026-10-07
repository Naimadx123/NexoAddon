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
import com.nexomc.protectionlib.ProtectionLib;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.BedrockBreak;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BlockHardnessHandler implements PacketListener {

  private final Map<Location, ScheduledTask> breakingTasks = new ConcurrentHashMap<>();
  private final Map<Location, Integer> breakingProgress = new ConcurrentHashMap<>();

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {

    Player player = event.getPlayer();

    if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
    if (player == null) return;

    WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
    Vector3i position = digging.getBlockPosition();
    DiggingAction digType = digging.getAction();
    if(digType == null) return;
    player.getScheduler().run(NexoAddon.getInstance(), task -> {
      if (player.getGameMode() == GameMode.CREATIVE) return;
      Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());
      if (!Bukkit.isOwnedByCurrentRegion(location)) return;
      if (digType == DiggingAction.START_DIGGING) {
        handleStartBreak(player, location, digging);
      } else if (digType == DiggingAction.FINISHED_DIGGING ||
          digType == DiggingAction.CANCELLED_DIGGING) {
        handleStopBreak(location, digging);
      }
    }, null);
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
    Sound sound = bedrockBreak.sound();

    AtomicInteger progress = new AtomicInteger();
    AtomicReference<ScheduledTask> scheduled = new AtomicReference<>();
    ScheduledTask task = player.getScheduler().runAtFixedRate(NexoAddon.getInstance(), breaking -> {
      if (!Bukkit.isOwnedByCurrentRegion(location)) {
        stopBreaking(location, digging);
        return;
      }
      if (!block.getType().equals(Material.BEDROCK)) {
        stopBreaking(location, digging);
        return;
      }

      int lastStage = breakingProgress.getOrDefault(location, -1);
      int currentProgress = progress.incrementAndGet();
      breakingProgress.put(location, currentProgress);

      int newStage = getBreakStage(currentProgress, hardness);
      if (newStage != lastStage) {
        sendBlockBreakAnimation(location, newStage, digging);
      }

      if (currentProgress >= hardness) {
        stopBreaking(location, digging);
        if (EventUtil.callEvent(new BlockBreakEvent(block, player)) && ProtectionLib.canBreak(player, location)) {
          player.getScheduler().run(NexoAddon.getInstance(), finish -> {
            if (!Bukkit.isOwnedByCurrentRegion(location) || block.getType() != Material.BEDROCK) return;
            ItemStack currentTool = player.getInventory().getItemInMainHand();
            if(!toolId.equals(NexoItems.idFromItem(currentTool))) return;

            boolean toolBroke = false;
            if(currentTool.getItemMeta() instanceof Damageable damageable){
              damageable.setDamage(damageable.getDamage()+bedrockBreak.durabilityCost());
              int maxDurability = NexoItems.itemFromId(toolId).getMaxDamage() != null ? NexoItems.itemFromId(toolId).getMaxDamage() : NexoItems.itemFromId(toolId).build().getType().getMaxDurability();
              if(damageable.getDamage() >= maxDurability) {
                toolBroke = true;
              } else {
                currentTool.setItemMeta(damageable);
              }
            }

            if(Math.random() <= probability)
              block.getWorld().dropItemNaturally(location, new ItemStack(Material.BEDROCK));
            block.breakNaturally();
            if(sound != null) {
              block.getWorld().playSound(player.getLocation(), sound, 1f, 1f);
            }

            if(toolBroke) {
              player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
              block.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
          }, null);
        }
      }
    }, () -> {
      ScheduledTask retired = scheduled.get();
      if (retired != null && breakingTasks.remove(location, retired)) breakingProgress.remove(location);
    }, 1L, 10L);
    scheduled.set(task);
    if (task != null) {
      ScheduledTask previous = breakingTasks.put(location, task);
      if (previous != null) previous.cancel();
    }
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
    ScheduledTask task = breakingTasks.remove(location);
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
        if(NexoAddon.isDebug){
          NexoAddon.getInstance().getLogger().warning("Failed to send block break animation to player.\n" + e.getMessage() + "\n" + e.getStackTrace());
        }
      }
    }
  }
}
