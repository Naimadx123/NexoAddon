package zone.vao.nexoAddon.classes;

import lombok.Getter;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.classes.component.Equippable;
import zone.vao.nexoAddon.classes.component.Fertilizer;
import zone.vao.nexoAddon.classes.component.JukeboxPlayable;

import java.util.List;

@Getter
public class Components {

  private final String id;
  private Equippable equippable;
  private JukeboxPlayable playable;
  private Fertilizer fertilizer;

  public Components(String id) {
    this.id = id;
  }

  public void setEquippable(EquipmentSlot slot) {
    this.equippable = new Equippable(slot);
  }

  public void setPlayable(String songKey) {
    this.playable = new JukeboxPlayable(songKey);
  }

  public void setFertilizer(int growthSpeedup, List<String> usableOn) {
    this.fertilizer = new Fertilizer(growthSpeedup, usableOn);
  }
}

