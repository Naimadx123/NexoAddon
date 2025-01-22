package zone.vao.nexoAddon.events.playerInteracts;

import com.nexomc.nexo.api.NexoItems;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.mechanic.BigMining;

public class BigMiningToggle {

  private final static NamespacedKey key = new NamespacedKey(NexoAddon.getInstance(), "bigMiningSwitchable");

  public static void onToggle(final PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack tool = player.getInventory().getItemInMainHand();

    String toolId = NexoItems.idFromItem(tool);
    if (!BigMining.isBigMiningTool(toolId) || event.getHand() != EquipmentSlot.HAND) return;
    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

    BigMining bigMiningMechanic = NexoAddon.getInstance()
        .getMechanics()
        .get(toolId)
        .getBigMining();

    if (!bigMiningMechanic.isSwitchable() || tool.getItemMeta() == null) return;

    var meta = tool.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();

    if (!pdc.has(key, PersistentDataType.BOOLEAN)) {
      pdc.set(key, PersistentDataType.BOOLEAN, true);
      tool.setItemMeta(meta);
      turnOn(player, pdc);
      return;
    }

    boolean isOn = pdc.get(key, PersistentDataType.BOOLEAN);
    if (isOn) {
      turnOff(player, pdc);
    } else {
      turnOn(player, pdc);
    }

    tool.setItemMeta(meta);
  }

  private static void turnOff(final Player player, PersistentDataContainer pdc) {
    pdc.set(key, PersistentDataType.BOOLEAN, false);
    Audience.audience(player)
        .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig().getString("messages.bigmining.off", "<red>BigMining off")));
  }

  private static void turnOn(final Player player, PersistentDataContainer pdc) {
    pdc.set(key, PersistentDataType.BOOLEAN, true);

    Audience.audience(player)
        .sendActionBar(MiniMessage.miniMessage().deserialize(NexoAddon.getInstance().getGlobalConfig().getString("messages.bigmining.on", "<green>BigMining on")));
  }

}
