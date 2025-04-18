package zone.vao.nexoAddon.nms;

public class NmsHandlerFactory {
  public static INmsHandler create() {
    String version = org.bukkit.Bukkit.getServer().getMinecraftVersion();
    try {
      if (version.contains("1.20.4")) {
        return (INmsHandler) Class.forName("zone.vao.nexoAddon.nms.v1_20_4.NmsHandler").getDeclaredConstructor().newInstance();
      } else if (version.contains("1.20.6")) {
        return (INmsHandler) Class.forName("zone.vao.nexoAddon.nms.v1_20_6.NmsHandler").getDeclaredConstructor().newInstance();
      } else if (version.contains("1.21.4")) {
        return (INmsHandler) Class.forName("zone.vao.nexoAddon.nms.v1_21_4.NmsHandler").getDeclaredConstructor().newInstance();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    throw new UnsupportedOperationException("No NMS handler found for version: " + version);
  }
}
