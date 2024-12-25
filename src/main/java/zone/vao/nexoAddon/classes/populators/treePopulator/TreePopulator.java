package zone.vao.nexoAddon.classes.populators.treePopulator;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TreePopulator {
  List<CustomTree> trees = new ArrayList<>();

  public void addTree(CustomTree tree) {
    trees.add(tree);
  }

  public void removeTree(CustomTree tree) {
    trees.remove(tree);
  }

  public void clearTrees() {
    trees.clear();
  }
}
