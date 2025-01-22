package zone.vao.nexoAddon.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
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
import zone.vao.nexoAddon.classes.mechanic.BedrockBreak;
import zone.vao.nexoAddon.utils.EventUtil;

import java.util.HashMap;
import java.util.Map;

public class BlockHardnessHandler {

  private final Map<Location, BukkitTask> breakingTasks = new HashMap<>();
  private final Map<Location, Integer> breakingProgress = new HashMap<>();

  public void registerListener() {
    if (!NexoAddon.getInstance().isProtocolLibLoaded()) {
      return;
    }
    NexoAddon.getInstance().getProtocolManager().addPacketListener(new PacketAdapter(NexoAddon.getInstance(), PacketType.Play.Client.BLOCK_DIG) {
      @Override
      public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) return;

        BlockPosition position = packet.getBlockPositionModifier().read(0);
        EnumWrappers.PlayerDigType digType = null;
        try {
          digType = packet.getEnumModifier(EnumWrappers.PlayerDigType.class, 2).read(0);
        }catch(Exception ignored){}
        if(digType == null) return;
        Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());

        if (digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
          handleStartBreak(player, location);
        } else if (digType == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK ||
            digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
          handleStopBreak(location);
        }
      }
    });
  }

  private void handleStartBreak(Player player, Location location) {
    ItemStack tool = player.getInventory().getItemInMainHand();
    String toolId = NexoItems.idFromItem(tool);

    if (toolId == null
        || NexoAddon.getInstance().getMechanics().isEmpty()
        || NexoAddon.getInstance().getMechanics().get(toolId) == null
    ) return;

    BedrockBreak bedrockBreak = NexoAddon.getInstance().getMechanics().get(toolId).getBedrockBreak();
    if (bedrockBreak == null) return;

    Block block = location.getBlock();
    if (block.getType() != Material.BEDROCK || bedrockBreak.isDisableOnFirstLayer() && block.getY() <= block.getWorld().getMinHeight()) return;

    int hardness = bedrockBreak.getHardness();
    double probability = bedrockBreak.getProbability();

    BukkitScheduler scheduler = Bukkit.getScheduler();
    breakingTasks.put(location, scheduler.runTaskTimer(NexoAddon.getInstance(), new Runnable() {
      int progress = 0;

      @Override
      public void run() {
        if (!block.getType().equals(Material.BEDROCK)) {
          stopBreaking(location);
          return;
        }

        progress++;
        breakingProgress.put(location, progress);
        sendBlockBreakAnimation(location, progress);

        if (progress >= hardness) {
          stopBreaking(location);
          if (EventUtil.callEvent(new BlockBreakEvent(block, player)) && ProtectionLib.canBreak(player, location)) {
            Bukkit.getScheduler().runTask(NexoAddon.getInstance(), () -> {
              if(Math.random() <= probability)
                block.getWorld().dropItemNaturally(location, new ItemStack(Material.BEDROCK));
              block.breakNaturally();

              if(tool.getItemMeta() instanceof Damageable damageable){
                damageable.setDamage(damageable.getDamage()+bedrockBreak.getDurabilityCost());
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

  private void handleStopBreak(Location location) {
    stopBreaking(location);
  }

  private void stopBreaking(Location location) {
    BukkitTask task = breakingTasks.remove(location);
    if (task != null) {
      task.cancel();
    }
    breakingProgress.remove(location);
    sendBlockBreakAnimation(location, -1);
  }

  private void sendBlockBreakAnimation(Location location, int stage) {
    PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
    packet.getIntegers().write(0, location.hashCode());
    packet.getIntegers().write(1, stage);
    packet.getBlockPositionModifier().write(0, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));

    for (Player player : location.getWorld().getPlayers()) {
      try {
        NexoAddon.getInstance().getProtocolManager().sendServerPacket(player, packet);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
