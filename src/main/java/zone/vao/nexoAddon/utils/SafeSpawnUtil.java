package zone.vao.nexoAddon.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class SafeSpawnUtil {
    private static final int DEFAULT_H_RADIUS = 3;
    private static final int DEFAULT_V_RADIUS = 2;
    private static final int DEFAULT_CLEARANCE = 2;

    private SafeSpawnUtil() {}

    public static Optional<Location> findSafeSpawnAround(Block broken) {
        return findSafeSpawnAround(broken, DEFAULT_H_RADIUS, DEFAULT_V_RADIUS, DEFAULT_CLEARANCE);
    }

    public static Optional<Location> findSafeSpawnAround(Block broken, int hRadius, int vRadius) {
        return findSafeSpawnAround(broken, hRadius, vRadius, DEFAULT_CLEARANCE);
    }

    public static Optional<Location> findSafeSpawnAround(Block broken, Entity entity) {
        return findSafeSpawnAround(broken, DEFAULT_H_RADIUS, DEFAULT_V_RADIUS, (int) Math.ceil(entity.getHeight()));
    }

    public static Optional<Location> findSafeSpawnAround(Block broken, int hRadius, int vRadius, int clearanceBlocks) {
        Block startFeet = broken.getRelative(BlockFace.UP);
        List<int[]> offsets = new ArrayList<>();
        for (int dy = -vRadius; dy <= vRadius; dy++) {
            for (int dx = -hRadius; dx <= hRadius; dx++) {
                for (int dz = -hRadius; dz <= hRadius; dz++) {
                    offsets.add(new int[]{dx, dy, dz});
                }
            }
        }
        offsets.sort(Comparator.comparingInt(o -> o[0]*o[0] + o[1]*o[1] + o[2]*o[2]));

        for (int[] o : offsets) {
            Block feet = startFeet.getRelative(o[0], o[1], o[2]);
            if (isSafeColumn(feet, clearanceBlocks)) {
                return Optional.of(center(feet));
            }
        }

        for (int up = 1; up <= 5; up++) {
            Block feet = startFeet.getRelative(0, up, 0);
            if (isSafeColumn(feet, clearanceBlocks)) {
                return Optional.of(center(feet));
            }
        }

        return Optional.empty();
    }

    public static int clearanceFor(EntityType type) {
        if (type == null) return DEFAULT_CLEARANCE;
        switch (type) {
            case ENDERMAN: return 3;
            case SILVERFISH:
            case ENDERMITE: return 1;
            case CAVE_SPIDER:
            case FOX:
            case BEE:
            case CAT:
            case PARROT:
            case RABBIT:
            case TROPICAL_FISH:
            case COD:
            case SALMON:
            case PUFFERFISH: return 1; // small mobs
            case SPIDER:
            case ZOMBIE:
            case HUSK:
            case DROWNED:
            case SKELETON:
            case STRAY:
            case WITHER_SKELETON:
            case PIGLIN:
            case PIGLIN_BRUTE:
            case ZOMBIFIED_PIGLIN:
            case VINDICATOR:
            case PILLAGER:
            case ILLUSIONER:
            case EVOKER:
            case VILLAGER:
            case WITCH:
            case PLAYER:
            default: return 2;
        }
    }

    private static boolean isSafeColumn(Block feet, int clearance) {
        if (!hasSolidGround(feet.getRelative(BlockFace.DOWN))) return false;
        for (int i = 0; i < clearance; i++) {
            Block b = feet.getRelative(0, i, 0);
            if (!isAirOrPassable(b)) return false;
            if (isDangerous(b)) return false;
        }
        return true;
    }

    private static boolean isAirOrPassable(Block b) {
        return b.isEmpty() || b.isPassable();
    }

    private static boolean hasSolidGround(Block below) {
        Material m = below.getType();
        if (m.isAir() || (!m.isSolid()) || m == Material.LADDER || m == Material.VINE || m == Material.SCAFFOLDING) return false;
        if (!below.isPassable()) return true;
        if (m.isSolid()) return true;
        return Tag.SLABS.isTagged(m) || Tag.STAIRS.isTagged(m);
    }

    private static boolean isDangerous(Block b) {
        Material m = b.getType();
        return m == Material.CACTUS
                || m == Material.LAVA
                || m == Material.FIRE
                || m == Material.CAMPFIRE
                || m == Material.SOUL_CAMPFIRE
                || m == Material.MAGMA_BLOCK
                || m == Material.SWEET_BERRY_BUSH;
    }

    private static Location center(Block b) {
        return b.getLocation().add(0.5, 0, 0.5);
    }
}
