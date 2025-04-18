package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.utils.drops.Drop;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.*;

public record Infested(List<EntityType> entities, List<String> mythicMobs, double probability, String selector, boolean particles, boolean drop) {

  public static class InfestedListener implements Listener {

    @EventHandler
    public static void onBreak(NexoNoteBlockBreakEvent event) {
      Mechanics mechanics = getMechanics(event);
      if (mechanics == null || shouldCancelEvent(event, mechanics)) return;

      Infested infested = mechanics.getInfested();
      if (Math.random() > infested.probability()) return;

      handleDrop(event, infested);
      if (infested.particles()) spawnParticles(event.getBlock());

      List<Object> entities = collectEntities(infested);
      spawnEntities(event, infested.selector(), entities);
    }

    private static Mechanics getMechanics(NexoNoteBlockBreakEvent event) {
      return NexoAddon.getInstance().getMechanics().getOrDefault(event.getMechanic().getItemID(), null);
    }

    private static boolean shouldCancelEvent(NexoNoteBlockBreakEvent event, Mechanics mechanics) {
      return event.isCancelled()
          || mechanics.getInfested() == null
          || !ProtectionLib.canBreak(event.getPlayer(), event.getBlock().getLocation())
          || event.getPlayer().getGameMode() == GameMode.CREATIVE;
    }

    private static void handleDrop(NexoNoteBlockBreakEvent event, Infested infested) {
      if (!infested.drop()) {
        event.setDrop(new Drop(
            Collections.emptyList(),
            false,
            false,
            Objects.requireNonNull(NexoBlocks.customBlockMechanic(event.getBlock().getLocation())).getItemID()
        ));
      }
    }

    private static void spawnParticles(Block block) {
      block.getWorld().spawnParticle(Particle.WHITE_SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 10);
    }

    private static List<Object> collectEntities(Infested infested) {
      List<Object> entities = new ArrayList<>(infested.entities());
      entities.addAll(infested.mythicMobs());
      return entities;
    }

    private static void spawnEntities(NexoNoteBlockBreakEvent event, String selector, List<Object> entities) {
      if (entities.isEmpty()) return;

      switch (selector.toLowerCase()) {
        case "all":
          entities.forEach(entity -> spawnEntityOrMythicMob(event, entity));
          break;
        case "random":
          Object randomEntity = entities.get(new Random().nextInt(entities.size()));
          spawnEntityOrMythicMob(event, randomEntity);
          break;
      }
    }

    private static void spawnEntityOrMythicMob(NexoNoteBlockBreakEvent event, Object entity) {
      if (entity instanceof EntityType) {
        spawnEntity(event, (EntityType) entity);
      } else if (entity instanceof String) {
        spawnMythicMob(event, (String) entity);
      }
    }

    private static void spawnEntity(NexoNoteBlockBreakEvent event, EntityType entityType) {
      event.getBlock().getWorld().spawnEntity(
          event.getBlock().getLocation().add(0.5, 0, 0.5),
          entityType
      );
    }

    private static void spawnMythicMob(NexoNoteBlockBreakEvent event, String mobName) {
      MythicBukkit mythicBukkit = MythicBukkit.inst();
      if (mythicBukkit.getMobManager().getMythicMob(mobName).isPresent()) {
        mythicBukkit.getMobManager().spawnMob(
            mobName,
            new Location(event.getBlock().getWorld(),
                event.getBlock().getX() + 0.5,
                event.getBlock().getY(),
                event.getBlock().getZ() + 0.5)
        );
      } else {
        NexoAddon.getInstance().getLogger().warning("MythicMob not found: " + mobName);
      }
    }
  }
}
