package zone.vao.nexoAddon.classes.bedrockbreak;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class BedrockBreakMechanicFactory extends MechanicFactory {

    private final boolean disabledOnFirstLayer;
    private final int durabilityCost;

    public BedrockBreakMechanicFactory(ConfigurationSection section) {
        super(section);
        disabledOnFirstLayer = section.getBoolean("disable_on_first_layer");
        durabilityCost = section.getInt("durability_cost");
        new BedrockBreakMechanicManager(this);
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        Mechanic mechanic = new BedrockBreakMechanic(this, itemMechanicConfiguration);
        addToImplemented(mechanic);
        return mechanic;
    }

    public boolean isDisabledOnFirstLayer() {
        return disabledOnFirstLayer;
    }

    public int getDurabilityCost() {
        return durabilityCost;
    }
}