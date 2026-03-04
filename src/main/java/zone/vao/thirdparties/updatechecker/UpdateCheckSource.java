package zone.vao.thirdparties.updatechecker;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents the source from where to fetch update information.
 */
public enum UpdateCheckSource {
    /**
     * SpigotMC API. Trustworthy, but slow. Requires the SpigotMC resource ID (the number at the end of your plugin's SpigotMC URL) as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}.
     */
    SPIGOT,
    /**
     * Polymart API. Trustworthy, but slow. Requires the Polymart resource ID (the number at the end of your plugin's Polymart URL) as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}.
     */
    POLYMART,
    /**
     * Spiget API. Not official, but faster than SpigotMC API. Requires the SpigotMC resource ID (the number at the end of your plugin's SpigotMC URL) as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}.
     */
    SPIGET,
    /**
     * GitHub Releases API. Requires your repository in the format "UserName/RepositoryName" (for example: "JEFF-Media-GbR/ChestSort") as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}. It will use the latest release's tag string.
     */
    GITHUB_RELEASE_TAG,
    /**
     * Hangar API. Requires your resource in the format "UserName/ProjectName/ReleaseChannel" (for example: "JEFF-Media-GbR/ChestSort/Release") as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}. It will use the latest release's version string according to that release channel.
     */
    HANGAR,
    /**
     * Custom link on where to fetch update checking information. Requires an HTTP or HTTPS URL as parameter in {@link UpdateChecker#UpdateChecker(JavaPlugin, UpdateCheckSource, String)}. The linked file must be a plaintext file containing only for the latest version string.
     */
    CUSTOM_URL
}