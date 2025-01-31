package zone.vao.nexoAddon.events.playerNexoBlockBreaks;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;

public class DropExperienceListener {

    public static void onBreak(NexoNoteBlockBreakEvent event) {
        if(NexoAddon.getInstance().getMechanics().isEmpty()) return;

        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
        if (mechanics == null || mechanics.getDropExperience() == null) return;

        double experience = mechanics.getDropExperience().experience();

        boolean isSilktouch = event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH);

        if (!event.isCancelled() && !(event.getPlayer().getGameMode() == GameMode.CREATIVE) && !isSilktouch) {
            Location location = event.getBlock().getLocation();
            if (location.getWorld() != null) {
                ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
                orb.setExperience((int) Math.floor(experience));
            }
        }
    }
}
