package zone.vao.nexoAddon.commands.repopulate;

import org.bukkit.Chunk;
import org.bukkit.World;
import zone.vao.nexoAddon.NexoAddon;

import static zone.vao.nexoAddon.events.chunk.FurniturePopulator.furniturePopulators;
import static zone.vao.nexoAddon.events.chunk.FurniturePopulator.processOre;

public class FurnitureRepopulator {

    public static void repopulate(World world, Chunk chunk) {
        NexoAddon.getInstance().getFoliaLib().getScheduler().runNextTick(populateSync -> {
            furniturePopulators.forEach(ore -> processOre(world, chunk, ore));
        });
    }
}
