package zone.vao.nexoAddon.events.playerNexoBlockBreaks;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;

import java.util.List;
import java.util.Random;

public class InfestedListener {
    public static void onBreak(NexoNoteBlockBreakEvent event) {
        if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
        if (event.isCancelled()
            || mechanics == null
            || mechanics.getInfested() == null
            || !ProtectionLib.canBreak(event.getPlayer(),event.getBlock().getLocation())
            || event.getPlayer().getGameMode() == GameMode.CREATIVE
        ) return;

        double probability = mechanics.getInfested().getProbability();
        if (Math.random() > probability) return;

        List<EntityType> entities = mechanics.getInfested().getEntities();
        String selector = mechanics.getInfested().getSelector();

        boolean spawnParticles = mechanics.getInfested().isParticles();
        if (spawnParticles) spawnParticles(event.getBlock());

        if (selector.equalsIgnoreCase("all")) {
            for (EntityType entityType : entities) {
                spawnEntity(event, entityType);
            }
        } else if (selector.equalsIgnoreCase("random")) {
            EntityType randomEntity = entities.get(new Random().nextInt(entities.size()));
            spawnEntity(event, randomEntity);
        }
    }

    private static void spawnEntity(NexoNoteBlockBreakEvent event, EntityType entityType) {
        event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0.5, 0, 0.5), entityType);
    }

    private static void spawnParticles(Block block) {
        block.getWorld().spawnParticle(Particle.WHITE_SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 10);
    }
}
