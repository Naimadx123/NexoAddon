package zone.vao.nexoAddon.utils.handlers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EquippableCompat {
  private EquippableCompat() {}

  private static final class Methods {
    Method has;
    Method get;
    Method set;
    Class<?> paramType;
  }

  private static final Map<Class<?>, Methods> CACHE = new ConcurrentHashMap<>();

  private static Methods resolve(Object meta) {
    if (meta == null) return new Methods();
    return CACHE.computeIfAbsent(meta.getClass(), cls -> {
      Methods m = new Methods();
      for (Method method : cls.getMethods()) {
        switch (method.getName()) {
          case "hasEquippable":
            if (method.getParameterCount() == 0) m.has = method;
            break;
          case "getEquippable":
            if (method.getParameterCount() == 0) m.get = method;
            break;
          case "setEquippable":
            if (method.getParameterCount() == 1) {
              m.set = method;
              m.paramType = method.getParameterTypes()[0];
            }
            break;
        }
      }
      try { if (m.has != null) m.has.setAccessible(true); } catch (Exception ignored) {}
      try { if (m.get != null) m.get.setAccessible(true); } catch (Exception ignored) {}
      try { if (m.set != null) m.set.setAccessible(true); } catch (Exception ignored) {}
      return m;
    });
  }

  public static boolean hasEquippable(Object meta) {
    Methods m = resolve(meta);
    if (m.has == null) return false;
    try {
      Object res = m.has.invoke(meta);
      return res instanceof Boolean && (Boolean) res;
    } catch (ReflectiveOperationException e) {
      return false;
    }
  }

  public static Object getEquippable(Object meta) {
    Methods m = resolve(meta);
    if (m.get == null) return null;
    try {
      return m.get.invoke(meta);
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }

  public static boolean setEquippable(Object meta, Object equippable) {
    Methods m = resolve(meta);
    if (m.set == null) return false;
    if (equippable != null && m.paramType != null && !m.paramType.isInstance(equippable)) {
      return false;
    }
    try {
      m.set.invoke(meta, equippable);
      return true;
    } catch (ReflectiveOperationException e) {
      return false;
    }
  }
}
