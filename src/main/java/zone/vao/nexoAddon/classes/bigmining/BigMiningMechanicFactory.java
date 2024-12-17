package zone.vao.nexoAddon.classes.bigmining;

import com.nexomc.nexo.NexoPlugin;
import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.utils.NexoYaml;
import com.nexomc.nexo.utils.PluginUtils;
import com.nexomc.nexo.utils.logs.Logs;
import org.bukkit.configuration.ConfigurationSection;

public class BigMiningMechanicFactory extends MechanicFactory {

    private final boolean callEvents;

    public BigMiningMechanicFactory(ConfigurationSection section) {
        super(section);
        if (PluginUtils.isEnabled("AdvancedEnchantments") && section.getBoolean("call_events", true)) {
            Logs.logError("AdvancedEnchantment is enabled, disabling BigMining-Mechanic");
            section.set("call_events", false);
            NexoYaml.saveConfig(NexoPlugin.get().getDataFolder().toPath().resolve("mechanics.yml").toFile(), section);
            this.callEvents = false;
        } else this.callEvents = section.getBoolean("call_events", true);
        MechanicsManager.registerListeners(NexoPlugin.get(), getMechanicID(), new BigMiningMechanicListener(this));
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