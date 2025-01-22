package zone.vao.nexoAddon.handlers;

import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;

import java.lang.reflect.Method;

public class ApiCompatibilityHandler {

  public static boolean hasEvolution(FurnitureMechanic mechanic) {
    try {
      Method oldMethod = mechanic.getClass().getMethod("hasEvolution");
      return (boolean) oldMethod.invoke(mechanic);
    } catch (NoSuchMethodException e) {
      try {
        Method newMethod = mechanic.getClass().getMethod("getHasEvolution");
        return (boolean) newMethod.invoke(mechanic);
      } catch (Exception ex) {
        throw new RuntimeException("Neither 'hasEvolution()' nor 'getHasEvolution()' are available!", ex);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error invoking method on mechanic", e);
    }
  }
}
