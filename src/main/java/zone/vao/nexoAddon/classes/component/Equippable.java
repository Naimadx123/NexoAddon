package zone.vao.nexoAddon.classes.component;

import lombok.Getter;
import org.bukkit.inventory.EquipmentSlot;

@Getter
public class Equippable{
  public EquipmentSlot slot;

  public Equippable(EquipmentSlot slot){
    this.slot = slot;
  }
}
