package zone.vao.nexoAddon.populators;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.WorldInfo;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PopulationEngine {

  private static final BlockFace[] SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
  private static final BlockFace[] NEIGHBOURS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

  public void populate(WorldInfo world, Random random, int chunkX, int chunkZ, PopulationRegion region, PopulatorDefinition rule) {
    if (!rule.appliesToWorld(world.getName()) || random.nextDouble() >= rule.chance()) return;
    int bottom = Math.max(world.getMinHeight(), rule.minY());
    int top = Math.min(world.getMaxHeight() - 1, rule.maxY());
    if (bottom > top) return;

    Set<Position> placed = new HashSet<>();
    if (rule.iterations().scansChunk()) {
      scan(random, chunkX, chunkZ, bottom, top, region, rule, placed);
      return;
    }

    int amount = rule.iterations().sample(random);
    long retries = amount * 80L;
    for (long attempt = 0; attempt < retries && placed.size() < amount; attempt++) {
      Position position = new Position((chunkX << 4) + random.nextInt(16), bottom + random.nextInt(top - bottom + 1),
          (chunkZ << 4) + random.nextInt(16));
      if (!place(position, random, region, rule, placed)) continue;
      if (random.nextDouble() >= rule.clusterChance()) continue;
      int size = Math.min(rule.veinSize().sample(random), amount - placed.size() + 1);
      if (size > 1) vein(position, size, random, chunkX, chunkZ, bottom, top, region, rule, placed);
    }
  }

  private void scan(Random random, int chunkX, int chunkZ, int bottom, int top, PopulationRegion region,
                    PopulatorDefinition rule, Set<Position> placed) {
    for (int x = chunkX << 4; x < (chunkX << 4) + 16; x++) {
      for (int z = chunkZ << 4; z < (chunkZ << 4) + 16; z++) {
        for (int y = bottom; y <= top; y++) place(new Position(x, y, z), random, region, rule, placed);
      }
    }
  }

  private void vein(Position start, int size, Random random, int chunkX, int chunkZ, int bottom, int top,
                    PopulationRegion region, PopulatorDefinition rule, Set<Position> placed) {
    ArrayDeque<Position> frontier = new ArrayDeque<>();
    Set<Position> visited = new HashSet<>();
    frontier.add(start);
    visited.add(start);
    int count = 1;
    while (!frontier.isEmpty() && count < size) {
      Position current = frontier.removeFirst();
      int offset = random.nextInt(NEIGHBOURS.length);
      for (int index = 0; index < NEIGHBOURS.length && count < size; index++) {
        Position next = current.relative(NEIGHBOURS[(index + offset) % NEIGHBOURS.length]);
        if ((next.x() >> 4) != chunkX || (next.z() >> 4) != chunkZ || next.y() < bottom || next.y() > top) continue;
        if (!visited.add(next) || !place(next, random, region, rule, placed)) continue;
        frontier.add(next);
        count++;
      }
    }
  }

  private boolean place(Position position, Random random, PopulationRegion region, PopulatorDefinition rule, Set<Position> placed) {
    if (placed.contains(position) || !region.contains(position.x(), position.y(), position.z())) return false;
    if (!rule.biomes().matches(region.getBiome(position.x(), position.y(), position.z()))) return false;
    if (rule.item().upperData() != null) {
      if (placed.contains(position.relative(BlockFace.DOWN))) return false;
      Position upper = position.relative(BlockFace.UP);
      if (placed.contains(upper) || !region.contains(upper.x(), upper.y(), upper.z())) return false;
      if (!region.getType(upper.x(), upper.y(), upper.z()).isAir()) return false;
    }
    BlockFace facing = facing(position, random, region, rule.placement());
    if (facing == null || !region.place(rule.item(), position.x(), position.y(), position.z(), facing)) return false;
    placed.add(position);
    return true;
  }

  private BlockFace facing(Position position, Random random, PopulationRegion region, PlacementRules rules) {
    Material type = region.getType(position.x(), position.y(), position.z());
    if (rules.replace().contains(type)) return BlockFace.UP;
    if (rules.airOnly() && !type.isAir()) return null;
    if (supports(position.relative(BlockFace.DOWN), region, rules.on())) return BlockFace.UP;
    if (supports(position.relative(BlockFace.UP), region, rules.below())) return BlockFace.DOWN;
    int offset = random.nextInt(SIDES.length);
    for (int index = 0; index < SIDES.length; index++) {
      BlockFace side = SIDES[(index + offset) % SIDES.length];
      if (supports(position.relative(side), region, rules.side())) return side.getOppositeFace();
    }
    return null;
  }

  private boolean supports(Position position, PopulationRegion region, java.util.List<Material> materials) {
    return !materials.isEmpty() && region.contains(position.x(), position.y(), position.z())
        && materials.contains(region.getType(position.x(), position.y(), position.z()));
  }

  private record Position(int x, int y, int z) {

    Position relative(BlockFace face) {
      return new Position(x + face.getModX(), y + face.getModY(), z + face.getModZ());
    }
  }
}
