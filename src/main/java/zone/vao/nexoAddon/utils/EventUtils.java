package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class EventUtils {
    public static boolean handleEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}