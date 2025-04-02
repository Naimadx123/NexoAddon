package zone.vao.nexoAddon.populators.orePopulator;

import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;

@Getter
public class Ore {

  public String id;
  public int maxLevel;
  public int minLevel;
  public CustomBlockMechanic nexoBlocks;
  public FurnitureMechanic nexoFurniture;
  public Material vanillaMaterial;
  public double chance;
  public List<Material> replace;
  public List<Material> placeOn;
  public List<Material> placeBelow;
  public List<World> worlds;
  public List<String> worldNames;
  public List<NamespacedKey> biomes;
  public boolean onlyAir;
  Object iterations;
  boolean tall;
  Object veinSize;
  double clusterChance;

  public Ore(String id, Material vanillaMaterial, int minLevel, int maxLevel, double chance, List<Material> replace, List<Material> placeOn, List<Material> placeBelow, List<World> worlds, List<String> worldNames, List<NamespacedKey> biomes, Object iterations, boolean tall, Object veinSize, double clusterChance, boolean onlyAir) {
    this.id = id;
    this.vanillaMaterial = vanillaMaterial;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.chance = chance;
    this.replace = replace;
    this.worlds = worlds;
    this.worldNames = worldNames;
    this.biomes = biomes;
    this.placeOn = placeOn;
    this.placeBelow = placeBelow;
    this.iterations = iterations;
    this.tall = tall;
    this.veinSize = veinSize;
    this.clusterChance = clusterChance;
    this.onlyAir = onlyAir;
  }

  public Ore(String id, FurnitureMechanic nexoFurniture, int minLevel, int maxLevel, double chance, List<Material> replace, List<Material> placeOn, List<Material> placeBelow, List<World> worlds, List<String> worldNames, List<NamespacedKey> biomes, Object iterations, boolean tall, Object veinSize, double clusterChance, boolean onlyAir) {
    this.id = id;
    this.nexoFurniture = nexoFurniture;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.chance = chance;
    this.replace = replace;
    this.worlds = worlds;
    this.worldNames = worldNames;
    this.biomes = biomes;
    this.placeOn = placeOn;
    this.placeBelow = placeBelow;
    this.iterations = iterations;
    this.tall = tall;
    this.veinSize = veinSize;
    this.clusterChance = clusterChance;
    this.onlyAir = onlyAir;
  }

  public Ore(String id, CustomBlockMechanic nexoBlocks, int minLevel, int maxLevel, double chance, List<Material> replace, List<Material> placeOn, List<Material> placeBelow, List<World> worlds, List<String> worldNames, List<NamespacedKey> biomes, Object iterations, boolean tall, Object veinSize, double clusterChance, boolean onlyAir) {
    this.id = id;
    this.nexoBlocks = nexoBlocks;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.chance = chance;
    this.replace = replace;
    this.worlds = worlds;
    this.worldNames = worldNames;
    this.biomes = biomes;
    this.placeOn = placeOn;
    this.placeBelow = placeBelow;
    this.iterations = iterations;
    this.tall = tall;
    this.veinSize = veinSize;
    this.clusterChance = clusterChance;
    this.onlyAir = onlyAir;
  }
}
