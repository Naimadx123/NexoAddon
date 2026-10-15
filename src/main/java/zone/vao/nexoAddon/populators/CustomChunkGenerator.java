package zone.vao.nexoAddon.populators;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CustomChunkGenerator extends ChunkGenerator {

  private final PopulatorManager manager;

  public CustomChunkGenerator(PopulatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    manager.generate(worldInfo, chunkX, chunkZ, chunkData);
  }

  @Override
  public boolean shouldGenerateNoise() {
    return true;
  }

  @Override
  public boolean shouldGenerateSurface() {
    return true;
  }

  @Override
  public boolean shouldGenerateBedrock() {
    return true;
  }

  @Override
  public boolean shouldGenerateCaves() {
    return true;
  }

  @Override
  public boolean shouldGenerateDecorations() {
    return true;
  }

  @Override
  public boolean shouldGenerateMobs() {
    return true;
  }

  @Override
  public boolean shouldGenerateStructures() {
    return true;
  }
}
