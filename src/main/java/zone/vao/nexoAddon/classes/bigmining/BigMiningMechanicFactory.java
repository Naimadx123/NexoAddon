package zone.vao.nexoAddon.classes.bigmining;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class BigMiningMechanicFactory extends MechanicFactory {
    private final boolean callEvents;

    public BigMiningMechanicFactory(ConfigurationSection section) {
        super(section);
        
        boolean shouldCallEvents = section.getBoolean("call_events", true);
        
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedEnchantments") && shouldCallEvents) {
            handleAdvancedEnchantmentsConflict(section);
            this.callEvents = false;
        } else {
            this.callEvents = shouldCallEvents;
        }

        registerMechanicListeners();
    }

    private void handleAdvancedEnchantmentsConflict(ConfigurationSection section) {
        Logs.logError("AdvancedEnchantment is enabled, disabling BigMining-Mechanic");
        section.set("call_events", false);
        
        FileConfiguration config = new YamlConfiguration();
        config.set("mechanics.bigmining", section);
        
        try {
            File mechanicsFile = new File(Bukkit.getPluginManager().getPlugin("Nexo").getDataFolder(), "mechanics.yml");
            config.save(mechanicsFile);
        } catch (IOException e) {
            Logs.logError("Failed to save mechanics configuration: " + e.getMessage());
        }
    }

    private void registerMechanicListeners() {
        MechanicsManager.INSTANCE.registerListeners(
            Bukkit.getPluginManager().getPlugin("NexoAddon"),
            getMechanicID(),
            new BigMiningMechanicListener(this)
        );
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new BigMiningMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }

    public boolean callEvents() {
        return callEvents;
    }
}