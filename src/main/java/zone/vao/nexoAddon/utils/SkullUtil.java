package zone.vao.nexoAddon.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Components;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkullUtil {

  public static String NEXOADDON_HEAD_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWY1ZTgzNWMxMTZlOGUyMDBlMmUwNmFhNTkzY2FiOGYxYTlmOGM0MGU3ZjAwNWE5Yzc2ZjEyZTI0ZjRjNjM3MCJ9fX0=";

  public static void applyTextures(){
    NexoAddon.getInstance().getComponents().forEach((key, component) -> {
      if(component.getSkullValue() == null && component.getNoteBlockSound() == null) return;
      ItemBuilder item = NexoItems.itemFromId(key);
      if (item == null || item.getType() != Material.PLAYER_HEAD) return;
      ItemStack itemStack = item.getFinalItemStack();
      if (itemStack == null) itemStack = item.regenerateItem().getFinalItemStack();
      if (itemStack != null) applyComponents(itemStack, component, key);
    });
  }

  static void applyComponents(ItemStack itemStack, Components component, String itemId) {
    if (!(itemStack.getItemMeta() instanceof SkullMeta meta)) return;
    if (component.getSkullValue() != null) {
      PlayerProfile profile = getProfileBase64(component.getSkullValue().value(), itemId);
      if (profile == null) return;
      meta.setPlayerProfile(profile);
    }
    if (component.getNoteBlockSound() != null) {
      trySetNoteBlockSound(meta, component.getNoteBlockSound().soundId());
    }
    itemStack.setItemMeta(meta);
  }

  private static void trySetNoteBlockSound(SkullMeta meta, String soundId) {
    if (soundId == null) return;

    try {
      NamespacedKey sound = NamespacedKey.fromString(soundId);
      if (sound == null || sound.getKey().isEmpty()) return;
      Method set = meta.getClass().getMethod("setNoteBlockSound", NamespacedKey.class);
      set.setAccessible(true);
      set.invoke(meta, sound);
    } catch (Throwable ignored) {}
  }

  private static PlayerProfile getProfileBase64(String base64, String itemId) {
    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
    PlayerTextures textures = profile.getTextures();
    URL urlObject;
    try {
      urlObject = getUrlFromBase64(base64);
    } catch (MalformedURLException exception) {
      NexoAddon.getInstance().getLogger().warning("Invalid base64: "+itemId);
      try {
        urlObject = getUrlFromBase64(NEXOADDON_HEAD_BASE64);
      } catch(MalformedURLException ignored) {
        return null;
      }
    }
    textures.setSkin(urlObject);
    profile.setTextures(textures);
    return profile;
  }

  public static URL getUrlFromBase64(String base64) throws MalformedURLException {
    try {
      String decoded = new String(Base64.getDecoder().decode(base64));
      JsonObject json = JsonParser.parseString(decoded).getAsJsonObject();
      String url = json.getAsJsonObject("textures")
          .getAsJsonObject("SKIN")
          .get("url")
          .getAsString();
      return URI.create(url).toURL();
    } catch (Throwable t) {
      throw new MalformedURLException("Invalid base64 string: " + base64);
    }
  }
}
