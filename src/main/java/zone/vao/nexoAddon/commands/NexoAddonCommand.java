package zone.vao.nexoAddon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.commands.repopulate.BlockRepopulator;
import zone.vao.nexoAddon.commands.repopulate.FurnitureRepopulator;
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
  @Syntax("<player> <customModelData|nexoID>")
  @CommandCompletion("@players @nexoItems")
  public void onTotem(CommandSender sender, String playerName, String input) {
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
      TotemUtil.playTotemAnimation(target, customModelData);
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Played totem animation with custom model data: " + customModelData));
    } catch (NumberFormatException e) {
      TotemUtil.playTotemAnimation(target, input);
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Played totem animation with Nexo item: " + input));
    }
  }
}