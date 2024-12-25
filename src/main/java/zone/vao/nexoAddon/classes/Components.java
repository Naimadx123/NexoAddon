package zone.vao.nexoAddon.classes;

import lombok.Getter;
import zone.vao.nexoAddon.classes.component.Equippable;
import zone.vao.nexoAddon.classes.component.JukeboxPlayable;

@Getter
public class Components {

  public String id;
  public Equippable equippable;
  public JukeboxPlayable playable;

  public Components(String id) {
    this.id = id;
  }

  public void setEquippable(String slot) {
    this.equippable = new Equippable(slot);
  }

  public void setPlayable(String songKey) {
    this.playable = new JukeboxPlayable(songKey);
  }

}

