package zone.vao.nexoAddon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.utils.TotemUtil;

@CommandAlias("nexoaddon")
@CommandPermission("nexoaddon.admin")
public class NexoAddonCommand extends BaseCommand {

  @Subcommand("reload")
  public void onReload(CommandSender sender) {
    NexoAddon.getInstance().reload();
    sender.sendMessage("Reloaded " + NexoAddon.getInstance().getName());
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