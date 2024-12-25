package zone.vao.nexoAddon.classes.component;

import lombok.Getter;

@Getter
public class JukeboxPlayable {
  String songKey;

  public JukeboxPlayable(String songKey) {
    this.songKey = songKey;
  }
}
