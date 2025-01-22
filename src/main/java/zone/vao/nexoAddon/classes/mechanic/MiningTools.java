package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public class MiningTools {
  private final List<Material> materials;
  private final List<String> nexoIds;
  private final String type;

  public MiningTools(final List<Material> materials, final List<String> nexoIds, String type) {
    this.materials = materials;
    this.nexoIds = nexoIds;
    this.type = type;
  }
}
