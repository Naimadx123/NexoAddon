package zone.vao.nexoAddon.classes.component;

import lombok.Getter;

import java.util.List;

@Getter
public class Fertilizer {
  private final int growthSpeedup;
  private final List<String> usableOn;

  public Fertilizer(int growthSpeedup, List<String> usableOn){
    this.growthSpeedup = growthSpeedup;
    this.usableOn = usableOn;
  }
}
