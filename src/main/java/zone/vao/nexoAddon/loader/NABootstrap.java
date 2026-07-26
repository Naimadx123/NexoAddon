package zone.vao.nexoAddon.loader;

import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.datapack.DiscoveredDatapack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import zone.vao.nexoAddon.biomes.BiomeGenerator;
import zone.vao.nexoAddon.biomes.CustomBiomeState;
import zone.vao.nexoAddon.biomes.SafeYaml;
import zone.vao.nexoAddon.biomes.VanillaBiomeSource;

import java.nio.file.Files;
import java.nio.file.Path;

public class NABootstrap implements PluginBootstrap {

  private static final String PACK_ID = "nexoaddon_biomes";

  @Override
  public void bootstrap(@NotNull BootstrapContext context) {
    ComponentLogger logger = context.getLogger();
    CustomBiomeState.markBootstrapRan();

    Path packDir;
    try {
      if ("off".equalsIgnoreCase(System.getProperty("nexoaddon.biomes", "on"))) {
        logger.warn("Custom biomes disabled via -Dnexoaddon.biomes=off.");
        return;
      }

      Path dataDir = context.getDataDirectory();
      Files.createDirectories(dataDir);

      YamlConfiguration config = SafeYaml.loadQuietly(dataDir.resolve("config.yml"));
      if (!config.getBoolean("custom_biomes.enabled", true)) return;

      BiomeGenerator generator = new BiomeGenerator(
          dataDir,
          config.getString("custom_biomes.namespace", "nexoaddon"),
          config.getString("custom_biomes.default_inherit", VanillaBiomeSource.DEFAULT_BASE),
          config.getBoolean("custom_biomes.prune_orphans", false)
      );

      BiomeGenerator.Result result = generator.run();
      CustomBiomeState.record(dataDir, result);
      result.warnings().forEach(logger::warn);

      long enabled = result.definitions().stream().filter(d -> d.enabled()).count();
      if (enabled == 0 && !Files.isRegularFile(result.packDir().resolve("pack.mcmeta"))) return;

      logger.info("Custom biomes: {} enabled, {} file(s) updated.", enabled, result.written());
      packDir = result.packDir();
    } catch (Throwable throwable) {
      logger.error("Custom biome generation failed; no custom biomes will be registered.", throwable);
      return;
    }

    try {
      context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY,
          event -> discover(event.registrar(), packDir, logger));
    } catch (Throwable throwable) {
      logger.error("Could not hook datapack discovery; custom biomes will not be registered.", throwable);
    }
  }

  private void discover(DatapackRegistrar registrar, Path packDir, ComponentLogger logger) {
    try {
      if (registrar.hasPackDiscovered(PACK_ID)) return;

      DiscoveredDatapack pack = registrar.discoverPack(packDir, PACK_ID, configurer -> configurer
          .autoEnableOnServerStart(true)
          .title(Component.text("NexoAddon Biomes")));

      CustomBiomeState.recordCompatibility(String.valueOf(pack.getCompatibility()));
      if (pack.getCompatibility() != Datapack.Compatibility.COMPATIBLE) {
        logger.error("The generated biome datapack was rejected as {} — wrote {}. "
                + "Custom biomes will NOT be registered; please report this with your server version.",
            pack.getCompatibility(), CustomBiomeState.formatSummary());
      }
    } catch (Throwable throwable) {
      logger.error("Failed to register the custom biome datapack.", throwable);
    }
  }
}
