package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;

public class VersionUtil {

  public static boolean isVersionLessThan(String targetVersion) {
    String version = Bukkit.getBukkitVersion();
    return checkVersion(targetVersion, version);
  }

  public static boolean nexoVersionLessThan(String targetVersion) {
    String version = Bukkit.getPluginManager().getPlugin("Nexo").getDescription().getVersion();
    return checkVersion(targetVersion, version);
  }

  private static boolean checkVersion(String targetVersion, String version) {
    String[] versionParts = version.split("-")[0].split("\\.");
    String[] targetVersionParts = targetVersion.split("\\.");

    int length = Math.max(versionParts.length, targetVersionParts.length);
    int[] currentVersionNumbers = new int[length];
    int[] targetVersionNumbers = new int[length];

    for (int i = 0; i < length; i++) {
      currentVersionNumbers[i] = (i < versionParts.length) ? Integer.parseInt(versionParts[i]) : 0;
      targetVersionNumbers[i] = (i < targetVersionParts.length) ? Integer.parseInt(targetVersionParts[i]) : 0;
    }

    for (int i = 0; i < length; i++) {
      if (currentVersionNumbers[i] < targetVersionNumbers[i]) {
        return true;
      } else if (currentVersionNumbers[i] > targetVersionNumbers[i]) {
        return false;
      }
    }
    return false;
  }
}
