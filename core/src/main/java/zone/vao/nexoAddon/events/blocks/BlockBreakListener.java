package zone.vao.nexoAddon.events.blocks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import zone.vao.nexoAddon.events.blocks.breaks.ShearsBreak;

public class BlockBreakListener implements Listener {

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    ShearsBreak.onBlockBreak(event);
  }
}
