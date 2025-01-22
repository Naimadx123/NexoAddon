package zone.vao.nexoAddon.events;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.events.playerNexoBlockBreaks.DropExperienceListener;
import zone.vao.nexoAddon.events.playerNexoBlockBreaks.InfestedListener;
import zone.vao.nexoAddon.events.playerNexoBlockBreaks.MiningToolsListener;

public class NexoBlockBreakListener implements Listener {

  @EventHandler
  public void onBlockBreak(NexoNoteBlockBreakEvent event) {

    MiningToolsListener.onBreak(event);
    DropExperienceListener.onBreak(event);
    InfestedListener.onBreak(event);
  }
}
