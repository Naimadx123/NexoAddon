package zone.vao.nexoAddon.classes;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import zone.vao.nexoAddon.classes.mechanic.*;

import java.util.List;

@Getter
public class Mechanics {

  private final String id;
  private Repair repair;
  private BigMining bigMining;
  private BedrockBreak bedrockBreak;
  private Aura aura;

  private SpawnerBreak spawnerBreak;
  private MiningTools miningTools;
  private DropExperience dropExperience;
  private Infested infested;

  public Mechanics(String id) {
    this.id = id;
  }

  public void setRepair(double ration, int fixedAmount) {
    this.repair = new Repair(ration, fixedAmount);
  }

  public void setBigMining(int radius, int depth, boolean switchable) {
    this.bigMining = new BigMining(radius, depth, switchable);
  }

  public void setBedrockBreak(int hardness, double probability, int durabilityCost, boolean disableOnFirstLayer) {
    this.bedrockBreak = new BedrockBreak(hardness, probability, durabilityCost, disableOnFirstLayer);
  }

  public void setAura(Particle particle, String type, String formula) {
    this.aura = new Aura(particle, type, formula);
  }

  public void setMiningTools(final List<Material> materials, final List<String> nexoIds, final String type) {
    this.miningTools = new MiningTools(materials, nexoIds, type);
  }

  public void setSpawnerBreak(double probability, boolean dropExperience) {
    this.spawnerBreak = new SpawnerBreak(probability, dropExperience);
  }

  public void setDropExperience(double experience) {
    this.dropExperience = new DropExperience(experience);
  }

  public void setInfested(List<EntityType> entities, double probability, String selector, boolean particles) {
    this.infested = new Infested(entities, probability, selector, particles);
  }
}
