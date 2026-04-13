package zone.vao.nexoAddon.commands.repopulate;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.LimitedRegion;
import zone.vao.nexoAddon.NexoAddon;

import java.util.Random;

public class BlockRepopulator {

    public static void repopulate(World world, Chunk chunk) {
        if (!NexoAddon.getInstance().worldPopulators.containsKey(world.getName())) {
            return;
        }

        NexoAddon.getInstance().worldPopulators.get(world.getName()).forEach(populator -> {
            if (NexoAddon.isDebug) {
                NexoAddon.getInstance().getLogger().info("[debug]  Repopulating chunk: " + chunk.getX() + ", " + chunk.getZ());
            }

            NexoAddon.getInstance().getFoliaLib().getScheduler().runNextTick(populateSync -> {
                LimitedRegion region = createLimitedRegion(world, chunk);
                if (region == null || populator.worldInfo == null) {
                    if (NexoAddon.isDebug) {
                        NexoAddon.getInstance().getLogger().info("[debug]    Region or worldInfo is null. Cancelling repopulation for this chunk.");
                    }
                    return;
                }
                populator.populate(populator.worldInfo, new Random(), chunk.getX(), chunk.getZ(), region);
            });
            if (NexoAddon.isDebug) {
                NexoAddon.getInstance().getLogger().info("[debug]    Chunk repopulated.");
            }
        });
    }

    private static LimitedRegion createLimitedRegion(World world, Chunk chunk) {
        try {
            Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);

            Class<?> chunkPosClass = Class.forName("net.minecraft.world.level.ChunkPos");
            Object chunkPos = chunkPosClass
                    .getConstructor(int.class, int.class)
                    .newInstance(chunk.getX(), chunk.getZ());

            Class<?> clrClass = Class.forName("org.bukkit.craftbukkit.generator.CraftLimitedRegion");
            Object clr = clrClass
                    .getConstructor(
                            Class.forName("net.minecraft.world.level.WorldGenLevel"),
                            chunkPosClass
                    )
                    .newInstance(nmsWorld, chunkPos);

            return (LimitedRegion) clr;
        } catch (ClassNotFoundException e) {
            NexoAddon.getInstance().getLogger().warning("LimitedRegion classes not found on this version: " + e.getMessage());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            NexoAddon.getInstance().getLogger().warning("Failed to construct CraftLimitedRegion via reflection.");
        }
        return null;
    }
}
