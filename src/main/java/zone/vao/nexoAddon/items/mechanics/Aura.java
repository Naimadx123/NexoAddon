package zone.vao.nexoAddon.items.mechanics;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Particle;

public record Aura(Particle particle, String type, Expression[] custom, int points) {

  public static final String[] VARIABLES = {"x", "y", "z", "angle", "angle2", "yaw", "pitch", "time", "Math_PI"};

  public static Expression[] compile(String formula) {
    String[] components = splitComponents(formula);
    if (components == null) return null;

    Expression[] compiled = new Expression[3];
    for (int i = 0; i < 3; i++) {
      compiled[i] = new ExpressionBuilder(components[i]).variables(VARIABLES).build();
    }
    return compiled;
  }

  private static String[] splitComponents(String formula) {
    int firstComma = -1;
    int secondComma = -1;
    int depth = 0;

    for (int i = 0; i < formula.length(); i++) {
      char c = formula.charAt(i);
      if (c == '(') depth++;
      else if (c == ')') depth--;
      else if (c == ',' && depth == 0) {
        if (firstComma == -1) firstComma = i;
        else if (secondComma == -1) secondComma = i;
        else return null;
      }
    }

    if (firstComma == -1 || secondComma == -1) return null;
    return new String[]{
        formula.substring(0, firstComma),
        formula.substring(firstComma + 1, secondComma),
        formula.substring(secondComma + 1)
    };
  }
}
