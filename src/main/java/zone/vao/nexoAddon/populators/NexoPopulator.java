package zone.vao.nexoAddon.populators;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class NexoPopulator extends BlockPopulator {

  private final PopulatorManager manager;

  public NexoPopulator(PopulatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
    manager.populate(worldInfo, random, chunkX, chunkZ, limitedRegion);
  }
}
