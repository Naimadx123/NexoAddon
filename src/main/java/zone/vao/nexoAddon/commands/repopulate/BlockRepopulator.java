package zone.vao.nexoAddon.commands.repopulate;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.VersionUtil;

import java.lang.reflect.Constructor;
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
                WorldInfo worldInfo = populator.worldInfo != null ? populator.worldInfo : world;
                if (region == null) {
                    if (NexoAddon.isDebug) {
                        NexoAddon.getInstance().getLogger().info("[debug]    Cancelling repopulation for chunk " + chunk.getX() + ", " + chunk.getZ()
                                + " (region is null, serverVersion=" + org.bukkit.Bukkit.getBukkitVersion() + ")");
                    }
                    return;
                }
                populator.populate(worldInfo, new Random(), chunk.getX(), chunk.getZ(), region);
            });
            if (NexoAddon.isDebug) {
                NexoAddon.getInstance().getLogger().info("[debug]    Chunk repopulated.");
            }
        });
    }

    private static LimitedRegion createLimitedRegion(World world, Chunk chunk) {
        if (VersionUtil.isVersionLessThan("26.1")) {
            return createLimitedRegionLegacy(world, chunk);
        }
        return createLimitedRegionModern(world, chunk);
    }

    private static LimitedRegion createLimitedRegionLegacy(World world, Chunk chunk) {
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

    private static LimitedRegion createLimitedRegionModern(World world, Chunk chunk) {
        try {
            Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);

            Class<?> chunkPosClass = Class.forName("net.minecraft.world.level.ChunkPos");
            Constructor<?> chunkPosCtor = chunkPosClass.getDeclaredConstructor(int.class, int.class);
            chunkPosCtor.setAccessible(true);
            Object chunkPos = chunkPosCtor.newInstance(chunk.getX(), chunk.getZ());

            Class<?> clrClass = Class.forName("org.bukkit.craftbukkit.generator.CraftLimitedRegion");

            Constructor<?> ctor = null;
            for (Constructor<?> candidate : clrClass.getDeclaredConstructors()) {
                Class<?>[] params = candidate.getParameterTypes();
                if (params.length == 2
                        && params[0].isInstance(nmsWorld)
                        && params[1].isInstance(chunkPos)) {
                    ctor = candidate;
                    break;
                }
            }

            if (ctor == null) {
                NexoAddon.getInstance().getLogger().warning("No matching CraftLimitedRegion constructor found on this version.");
                return null;
            }

            ctor.setAccessible(true);
            return (LimitedRegion) ctor.newInstance(nmsWorld, chunkPos);
        } catch (ClassNotFoundException e) {
            NexoAddon.getInstance().getLogger().warning("LimitedRegion classes not found on this version: " + e.getMessage());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            NexoAddon.getInstance().getLogger().warning("Failed to construct CraftLimitedRegion via reflection (26.1+).");
        }
        return null;
    }
}
