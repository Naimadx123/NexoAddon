package zone.vao.nexoAddon.classes.populators;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import zone.vao.nexoAddon.classes.populators.orePopulator.CustomOrePopulator;
import zone.vao.nexoAddon.classes.populators.orePopulator.OrePopulator;

import java.util.List;

public class CustomChunkGenerator extends ChunkGenerator {

  private final OrePopulator orePopulator;

  public CustomChunkGenerator(OrePopulator orePopulator) {
    this.orePopulator = orePopulator;
  }

  @Override
  public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
    return List.of(new CustomOrePopulator(orePopulator));
  }
}
