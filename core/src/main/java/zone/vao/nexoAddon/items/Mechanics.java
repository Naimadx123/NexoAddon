package zone.vao.nexoAddon.items;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.mechanics.*;

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
  private BottledExp bottledExp;
  private Unstackable unstackable;
  private BlockAura blockAura;
  private Signal signal;
  private Remember remember;

  public Mechanics(String id) {
    this.id = id;
  }

  public void setRepair(double ration, int fixedAmount, List<Material> materials, List<String> nexoIds, List<Material> materialsBlacklist, List<String> nexoIdsBlacklist) {
    this.repair = new Repair(ration, fixedAmount, materials, nexoIds, materialsBlacklist, nexoIdsBlacklist);
  }

  public void setBigMining(int radius, int depth, boolean switchable, List<Material> materials) {
    this.bigMining = new BigMining(radius, depth, switchable, materials);
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

  public void setShiftBlock(String replaceTo, int time, List<Material> materials, List<String> nexoIds, boolean onInteract, boolean onDestroy, boolean onPlace){this.shiftBlock = new ShiftBlock(replaceTo, time, materials, nexoIds, onInteract, onDestroy, onPlace);}

  public void setBottledExp(Double ration, int cost) {this.bottledExp = new BottledExp(ration, cost);}

  public void setUnstackable(String next, String give, List<Material> materials, List<String> nexoIds) {this.unstackable = new Unstackable(next, give, materials, nexoIds);}

  public void setBlockAura(Particle particle, String xOffset, String yOffset, String zOffset, int amount, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
    this.blockAura = new BlockAura(particle, xOffset, yOffset, zOffset, amount, deltaX, deltaY, deltaZ, speed, force);
  }

  public void setSignal(int radius, double channel, String role) {
    this.signal = new Signal(radius, channel, role);
  }

  public void setRemember(boolean isForRemember) {
    this.remember = new Remember(isForRemember);
  }

  public static void registerListeners(NexoAddon plugin){

    registerListener(new BigMining.BigMiningListener(), plugin);
    registerListener(new BlockAura.BlockAuraListener(), plugin);
    registerListener(new BottledExp.BottledExpListener(), plugin);

    registerListener(new Decay.DecayListener(), plugin);
    registerListener(new DropExperience.DropExperienceListener(), plugin);

    registerListener(new Infested.InfestedListener(), plugin);

    registerListener(new KillMessage.KillMessageListener(), plugin);

    registerListener(new MiningTools.MiningToolsListener(), plugin);

    registerListener(new Remember.RememberListener(), plugin);
    registerListener(new Repair.RepairListener(), plugin);

    registerListener(new ShiftBlock.ShiftBlockListener(), plugin);
    registerListener(new SpawnerBreak.SpawnerBreakListener(), plugin);
    registerListener(new Stackable.StackableListener(), plugin);

    registerListener(new Unstackable.UnstackableListener(), plugin);

    registerListener(new Signal.SignalListener(), plugin);
  }

  private static void registerListener(Listener listener, NexoAddon plugin){

    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
  }
}
