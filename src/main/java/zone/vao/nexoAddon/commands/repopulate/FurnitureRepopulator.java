package zone.vao.nexoAddon.commands.repopulate;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import zone.vao.nexoAddon.NexoAddon;

import static zone.vao.nexoAddon.events.chunk.FurniturePopulator.furniturePopulators;
import static zone.vao.nexoAddon.events.chunk.FurniturePopulator.processOre;

public class FurnitureRepopulator {

    public static void repopulate(World world, Chunk chunk) {
        Bukkit.getRegionScheduler().run(NexoAddon.getInstance(), world, chunk.getX(), chunk.getZ(), populateSync -> {
            furniturePopulators.forEach(ore -> processOre(world, chunk, ore));
        });
    }
}
