package zone.vao.nexoAddon.utils;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Components;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class ItemConfigUtil {

  public static HashMap<File, LinkedHashMap<String, ItemBuilder>> getItemConfigs(){

    return NexoPlugin.instance().configsManager().parseItemConfig();
  }

  public static Set<File> getItemFiles(){

    return NexoPlugin.instance().configsManager().parseItemConfig().keySet();
  }

  public static void loadComponents() {
    NexoAddon.getInstance().getComponents().clear();

    for (File itemFile : getItemFiles()) {
      YamlConfiguration file = YamlConfiguration.loadConfiguration(itemFile);

      for (String itemId : file.getKeys(false)) {
        if (!file.isConfigurationSection(itemId)) continue;

        ConfigurationSection itemSection = file.getConfigurationSection(itemId);
        if (itemSection != null && itemSection.contains("Components")) {

          if(!NexoAddon.getInstance().getComponents().containsKey(itemId))
            NexoAddon.getInstance().getComponents().put(itemId, new Components(itemId));
          Components component = NexoAddon.getInstance().getComponents().get(itemId);

          if(itemSection.contains("Components.equippable")) {
            String equippableSlot = itemSection.getString("Components.equippable.slot", "HEAD");
            component.setEquippable(equippableSlot);
          }

          if(itemSection.contains("Components.jukebox_playable") && itemSection.contains("Components.jukebox_playable.song_key")) {
            String songKey = itemSection.getString("Components.jukebox_playable.song_key");
            component.setPlayable(songKey);
          }
        }
      }
    }
  }

}
