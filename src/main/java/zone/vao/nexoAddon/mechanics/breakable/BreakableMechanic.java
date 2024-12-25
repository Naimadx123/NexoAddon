package zone.vao.nexoAddon.mechanics.breakable;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public abstract class BreakableMechanic extends Mechanic {
    public BreakableMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
    }
}
