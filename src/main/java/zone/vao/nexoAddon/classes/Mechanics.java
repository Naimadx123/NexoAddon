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
  private KillMessage killMessage;
  private Stackable stackable;
  private Decay decay;
  private ShiftBlock shiftBlock;

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

  public void setInfested(List<EntityType> entities, List<String> mythicMobs, double probability, String selector, boolean particles, boolean drop) {
    this.infested = new Infested(entities, mythicMobs, probability, selector, particles, drop);
  }
  
  public void setKillMessage(String deathMessage) {
    this.killMessage = new KillMessage(deathMessage);
  }
  
  public void setStackable(String next, String group){
    this.stackable = new Stackable(next, group);
  }

  public void setDecay(int time, double chance, List<Material> base, List<String> nexoBase, int radius){this.decay = new Decay(time, chance, base, nexoBase, radius);}

  public void setShiftBlock(String replaceTo, int time, List<Material> materials, List<String> nexoIds){this.shiftBlock = new ShiftBlock(replaceTo, time, materials, nexoIds);}
}
