package zone.vao.nexoAddon.populators.treePopulator;

import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

@Getter
public class CustomTree {
  String id;
  CustomBlockMechanic log;
  CustomBlockMechanic leaves;
  public int maxLevel;
  public int minLevel;
  public double chance;
  public List<World> worlds;
  public List<NamespacedKey> biomes;

  public CustomTree(String id, CustomBlockMechanic log, CustomBlockMechanic leaves, int minLevel, int maxLevel, double chance, List<World> worlds, List<NamespacedKey> biomes) {
    this.id = id;
    this.log = log;
    this.leaves = leaves;
    this.maxLevel = maxLevel;
    this.minLevel = minLevel;
    this.chance = chance;
    this.worlds = worlds;
    this.biomes = biomes;
  }
}
