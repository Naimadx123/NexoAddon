package zone.vao.nexoAddon.classes.bedrockbreak;

import com.nexomc.nexo.mechanics.Mechanic;
import org.bukkit.configuration.ConfigurationSection;

public class BedrockBreakMechanic extends Mechanic {
    private final BedrockBreakMechanicFactory factory;

    public BedrockBreakMechanic(BedrockBreakMechanicFactory factory, ConfigurationSection config) {
        super(factory, config);
        this.factory = factory;
    }

    public boolean isDisabledOnFirstLayer() {
        return factory.isDisabledOnFirstLayer();
    }

    public int getDurabilityCost() {
        return factory.getDurabilityCost();
    }
}