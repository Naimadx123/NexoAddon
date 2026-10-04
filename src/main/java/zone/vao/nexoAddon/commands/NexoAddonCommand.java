package zone.vao.nexoAddon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.biomes.BiomeDefinition;
import zone.vao.nexoAddon.biomes.CustomBiomeState;
import zone.vao.nexoAddon.commands.repopulate.BlockRepopulator;
import zone.vao.nexoAddon.commands.repopulate.FurnitureRepopulator;
import zone.vao.nexoAddon.utils.LiquidUtil;
import zone.vao.nexoAddon.utils.TotemUtil;

import java.util.List;

@CommandAlias("nexoaddon")
@CommandPermission("nexoaddon.admin")
public class NexoAddonCommand extends BaseCommand {

  @Subcommand("reload")
  public void onReload(CommandSender sender) {
    NexoAddon.getInstance().reload();
    sender.sendMessage("Reloaded " + NexoAddon.getInstance().getName());
  }

  @Subcommand("liquidclean")
  @Syntax("[radius] [biome]")
  public void onLiquidClean(CommandSender sender, @Optional Integer radiusArg, @Optional String biomeArg) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Run this in-game."));
      return;
    }
    if (!NexoAddon.getInstance().getIsLiquid()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No liquid mechanic is configured."));
      return;
    }

    Biome forced = null;
    if (biomeArg != null && !biomeArg.isBlank()) {
      forced = LiquidUtil.resolveBiome(biomeArg);
      if (forced == null) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown biome `" + biomeArg + "`."));
        return;
      }
    }

    int radius = Math.max(1, Math.min(radiusArg == null ? 16 : radiusArg, 64));
    sender.sendMessage(MiniMessage.miniMessage().deserialize(
        "<yellow>Scanning " + radius + " blocks for liquid biomes with no water left..."));

    Location center = player.getLocation();
    Biome target = forced;
    NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocation(center, task ->
        LiquidUtil.cleanup(center, radius, target, result -> {
          sender.sendMessage(MiniMessage.miniMessage().deserialize(
              "<green>Restored " + result.restored() + " biome cell(s) <gray>(each cell is 4x4x4 blocks)."));

          if (result.hadWater() > 0)
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Skipped " + result.hadWater()
                + " cell(s) that still contain water. Remove the water there first."));

          if (result.noOriginal() > 0)
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Skipped " + result.noOriginal()
                + " cell(s) with no known original biome. Re-run with an explicit biome, e.g. "
                + "<white>/nexoaddon liquidclean " + radius + " minecraft:plains<yellow>."));
        }));
  }

  @Subcommand("biomes")
  public void onBiomes(CommandSender sender) {
    if (!CustomBiomeState.bootstrapRan()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "<red>Custom biome generation did not run. It is disabled in config.yml, was turned off with "
              + "-Dnexoaddon.biomes=off, or failed at startup — check the server log."));
      return;
    }

    List<BiomeDefinition> definitions = CustomBiomeState.definitions();
    if (definitions.isEmpty()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "<yellow>No custom biomes defined. Add them in <white>plugins/NexoAddon/custom_biomes/<yellow>."));
      return;
    }

    sender.sendMessage(MiniMessage.miniMessage().deserialize(
        "<gray>Custom biomes <white>(" + definitions.size() + ")<gray>:"));

    for (BiomeDefinition definition : definitions) {
      boolean registered = LiquidUtil.resolveBiome(definition.id()) != null;
      String status = !definition.enabled()
          ? "<dark_gray>disabled"
          : registered ? "<green>registered" : "<red>needs restart";

      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "<gray>- <white>" + definition.id() + " <gray>· " + status
              + " <dark_gray>(" + definition.sourceFile() + ")"));
    }

    String compatibility = CustomBiomeState.packCompatibility();
    if (compatibility != null && !"COMPATIBLE".equals(compatibility)) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "<red>Datapack was rejected as " + compatibility + " — " + CustomBiomeState.formatSummary()));
    }
    if (NexoAddon.getInstance().isBiomesChangedSinceStartup()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "<yellow>Definitions have changed on disk since startup. Restart the server to apply them."));
    }
  }

  @Subcommand("repopulate")
  @Syntax("[worldName|#all] <knowTheExperimentalFeature>")
  @CommandCompletion("@worlds")
  public void onRepopulate(CommandSender sender, @Optional String worldName, @Optional Boolean knowTheExperimentalFeature) {

    if(knowTheExperimentalFeature == null || !knowTheExperimentalFeature){
      sender.sendMessage(MiniMessage.miniMessage()
          .deserialize("<red>Repopulating is a experimental feature! We suggest you to make a backup at 1st. Be aware and if you will find any issues, report it on discord server. If you have read this message, type 'true' at the end of the command.</red>"));
      return;
    }

    List<World> targetWorlds;
    if (worldName == null || worldName.equalsIgnoreCase("#all")) {
      targetWorlds = Bukkit.getWorlds();
    } else {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
        sender.sendMessage(MiniMessage.miniMessage()
            .deserialize("<red>World not found: " + worldName + "</red>"));
        return;
      }
      targetWorlds = List.of(world);
    }

    sender.sendMessage(MiniMessage.miniMessage()
        .deserialize("<yellow>Scheduling repopulation for loaded chunks...</yellow>"));

    NexoAddon.getInstance().getFoliaLib().getScheduler().runAsync(populate -> {
      int processedChunks = 0;

      for (World world : targetWorlds) {
        if(NexoAddon.isDebug){
          NexoAddon.getInstance().getLogger().info("[debug] Repopulating world: " + world.getName());
        }
        if (!NexoAddon.getInstance().worldPopulators.containsKey(world.getName())) {
          if(NexoAddon.isDebug){
            NexoAddon.getInstance().getLogger().info("[debug]   Skipping world: " + world.getName() + " because it has no populators.");
          }
          continue;
        }
        for (Chunk chunk : world.getLoadedChunks()) {
          if (!chunk.isGenerated()) continue;

          BlockRepopulator.repopulate(world, chunk);
          FurnitureRepopulator.repopulate(world, chunk);

          processedChunks++;
        }
      }

      final int finalProcessedChunks = processedChunks;
      NexoAddon.getInstance().getFoliaLib().getScheduler().runNextTick(mess -> sender.sendMessage(
          MiniMessage.miniMessage().deserialize(
              "<green>Repopulation scheduled for <white>" + finalProcessedChunks + "</white> chunks.</green>"
          )
      ));
    });
  }

  @Subcommand("totem")
  @Syntax("<player> <customModelData|nexoID> [sound]")
  @CommandCompletion("@players @nexoItems @sounds")
  public void onTotem(CommandSender sender, String playerName, String input, @Optional String sound) {
    Player target = Bukkit.getPlayer(playerName);

    if (target == null) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
      return;
    }

    if (!NexoAddon.getInstance().isPacketEventsPresent()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>PacketEvents is required for this command!"));
      return;
    }

    try {
      int customModelData = Integer.parseInt(input);
      TotemUtil.playTotemAnimation(target, customModelData, sound);
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Played totem animation with custom model data: " + customModelData));
    } catch (NumberFormatException e) {
      TotemUtil.playTotemAnimation(target, input, sound);
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Played totem animation with Nexo item: " + input));
    }
  }
}