package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.utils.drops.Drop;
import com.nexomc.protectionlib.ProtectionLib;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.SafeSpawnUtil;

import java.util.*;

public record Infested(List<EntityType> entities, List<String> mythicMobs, double probability, String selector, boolean particles, boolean drop, boolean safeSpawn) {

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
            spawnEntities(event, infested.selector(), entities, infested.safeSpawn());
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

        private static void spawnEntities(NexoNoteBlockBreakEvent event, String selector, List<Object> entities, boolean safeSpawn) {
            if (entities.isEmpty()) return;

            switch (selector.toLowerCase()) {
                case "all":
                    entities.forEach(entity -> spawnEntityOrMythicMob(event, entity, safeSpawn));
                    break;
                case "random":
                default:
                    Object randomEntity = entities.get(new Random().nextInt(entities.size()));
                    spawnEntityOrMythicMob(event, randomEntity, safeSpawn);
                    break;
            }
        }

        private static void spawnEntityOrMythicMob(NexoNoteBlockBreakEvent event, Object entity, boolean safeSpawn) {
            if (entity instanceof EntityType type) {
                spawnEntity(event, type, safeSpawn);
            } else if (entity instanceof String mobName) {
                spawnMythicMob(event, mobName, safeSpawn);
            }
        }

        private static void spawnEntity(NexoNoteBlockBreakEvent event, EntityType entityType, boolean safeSpawn) {
            Location def = event.getBlock().getLocation().add(0.5, 0, 0.5);
            Entity e = null;
            if(safeSpawn)
                e = def.getWorld().spawnEntity(def, entityType);
            Location spawnLoc = safeSpawn ? SafeSpawnUtil.findSafeSpawnAround(event.getBlock(), e.copy()).orElse(def) : def;
            if(safeSpawn)
                e.remove();
            event.getBlock().getWorld().spawnEntity(spawnLoc, entityType);
        }

        private static void spawnMythicMob(NexoNoteBlockBreakEvent event, String mobName, boolean safeSpawn) {
            MythicBukkit mythic = MythicBukkit.inst();
            if (mythic.getMobManager().getMythicMob(mobName).isPresent()) {
                Location def = event.getBlock().getLocation().add(0.5, 0, 0.5);
                Location spawnLoc = safeSpawn ? SafeSpawnUtil.findSafeSpawnAround(event.getBlock()).orElse(def) : def;
                mythic.getMobManager().spawnMob(mobName, spawnLoc);
            } else {
                NexoAddon.getInstance().getLogger().warning("MythicMob not found: " + mobName);
            }
        }
    }
}
