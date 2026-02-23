package zone.vao.thirdparties.updatechecker;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserAgentBuilder {

    private final StringBuilder builder = new StringBuilder("JEFF-Media-GbR-SpigotUpdateChecker/").append(UpdateChecker.VERSION);
    private final UpdateChecker instance = UpdateChecker.getInstance();
    private final List<String> list = new ArrayList<>();
    private final Plugin plugin = instance.getPlugin();

    public static UserAgentBuilder getDefaultUserAgent() {
        return new UserAgentBuilder().addPluginNameAndVersion().addServerVersion().addBukkitVersion();
    }

    public UserAgentBuilder addBukkitVersion() {
        list.add("BukkitVersion/" + Bukkit.getBukkitVersion());
        return this;
    }

      public UserAgentBuilder addKeyValue(String key, String value) {
        list.add(key + "/" + value);
        return this;
    }

    public UserAgentBuilder addPlaintext(String text) {
        list.add(text);
        return this;
    }

    public UserAgentBuilder addPluginNameAndVersion() {
        list.add(plugin.getName() + "/" + plugin.getDescription().getVersion());
        return this;
    }

    public UserAgentBuilder addServerVersion() {
        list.add("ServerVersion/" + Bukkit.getVersion());
        return this;
    }

       public UserAgentBuilder addSpigotUserId() {
        String uid = instance.isUsingPaidVersion() ? instance.getSpigotUserId() : "none";
        list.add("SpigotUID/" + uid);
        return this;
    }

    public UserAgentBuilder addUsingPaidVersion() {
        list.add("Paid/" + instance.isUsingPaidVersion());
        return this;
    }

    protected String build() {
        if (list.size() > 0) {
            builder.append(" (");
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }
        return builder.toString();
    }

}
