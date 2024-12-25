package zone.vao.nexoAddon.classes.populators.orePopulator;

import java.util.Objects;
import java.util.UUID;

public class ChunkPosition {

  private final int chunkX;
  private final int chunkZ;
  private final UUID worldUID;

  public ChunkPosition(int chunkX, int chunkZ, UUID worldUID) {
    this.chunkX = chunkX;
    this.chunkZ = chunkZ;
    this.worldUID = worldUID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChunkPosition that = (ChunkPosition) o;
    return chunkX == that.chunkX && chunkZ == that.chunkZ && Objects.equals(worldUID, that.worldUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chunkX, chunkZ, worldUID);
  }
}
