package zone.vao.nexoAddon.items;

import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.components.Equippable;
import zone.vao.nexoAddon.items.components.Fertilizer;
import zone.vao.nexoAddon.items.components.JukeboxPlayable;
import zone.vao.nexoAddon.items.components.SkullValue;

import java.util.List;

@Getter
public class Components {

  private final String id;
  private Equippable equippable;
  private JukeboxPlayable playable;
  private Fertilizer fertilizer;
  private SkullValue skullValue;

  public Components(String id) {
    this.id = id;
  }

  public void setEquippable(EquipmentSlot slot) {
    this.equippable = new Equippable(slot);
  }

  public void setPlayable(String songKey) {
    this.playable = new JukeboxPlayable(songKey);
  }

  public void setFertilizer(int growthSpeedup, List<String> usableOn, int cooldown) {this.fertilizer = new Fertilizer(growthSpeedup, usableOn,cooldown);}

  public void setSkullValue(String value){this.skullValue = new SkullValue(value);}

  public static void registerListeners(NexoAddon plugin){

    registerListener(new Equippable.EquippableListener(), plugin);

    registerListener(new Fertilizer.FertilizerListener(), plugin);

    registerListener(new JukeboxPlayable.JukeboxPlayableListener(), plugin);
  }

  private static void registerListener(Listener listener, NexoAddon plugin){

    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
  }
}

