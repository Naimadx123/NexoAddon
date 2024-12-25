package zone.vao.nexoAddon.classes.populators.orePopulator;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OrePopulator {

  public List<Ore> ores = new ArrayList<>();

  public void addOre(Ore ore) {

    ores.add(ore);
  }

  public void removeOre(Ore ore) {
    ores.remove(ore);
  }


  public void clearOres() {
    ores.clear();
  }
}
