package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import zone.vao.nexoAddon.utils.BlockUtil;

import java.util.List;

public record Decay(int time, double chance, List<Material> base, List<String> nexoBase, int radius) {

  public static class DecayListener implements Listener {

    @EventHandler
    public void onNexoBlockPlace(NexoBlockPlaceEvent event) {

      BlockUtil.startDecay(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

      BlockUtil.startDecay(event.getBlock().getLocation());
    }

    @EventHandler
    public void onNexoBlockBreak(NexoBlockBreakEvent event) {

      BlockUtil.startDecay(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

      BlockUtil.startDecay(event.getBlock().getLocation());
    }
  }
}
